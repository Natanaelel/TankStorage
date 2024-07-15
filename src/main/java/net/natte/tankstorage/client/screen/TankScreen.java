package net.natte.tankstorage.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.natte.tankstorage.client.TankStorageClient;
import net.natte.tankstorage.client.helpers.FluidHelper;
import net.natte.tankstorage.client.rendering.FluidRenderer;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.gui.FluidSlot;
import net.natte.tankstorage.screenhandler.TankMenu;
import net.natte.tankstorage.util.Texts;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class TankScreen extends AbstractContainerScreen<TankMenu> {

    private static final ResourceLocation WIDGETS_TEXTURE = Util.ID("textures/gui/widgets.png");

    private final TankType type;
    private final ResourceLocation texture;
    private boolean isLockSlotKeyDown = false;

    public TankScreen(TankMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.type = handler.getTankType();

        this.texture = Util.ID("textures/gui/" + this.type.width() + "x" + this.type.height() + ".png");
        this.imageHeight = 114 + this.type.height() * 18;
        this.inventoryLabelY += this.type.height() * 18 - 52;
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(
                new InsertModeButtonWidget(Util.getInsertMode(this.menu.getTankItem()), leftPos + titleLabelX + this.imageWidth - 31, topPos + titleLabelY - 4,
                        14, 14, 14, WIDGETS_TEXTURE));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float tickDelta, int mouseX, int mouseY) {
        guiGraphics.blit(this.texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.type.guiTextureWidth, this.type.guiTextureHeight);

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.setFocused(null);
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (!(slot instanceof FluidSlot fluidSlot)) {
            super.renderSlot(guiGraphics, slot);
            return;
        }

        FluidStack fluidVariant = fluidSlot.getFluid();
        if (!fluidVariant.isEmpty()) {
            FluidRenderer.drawFluidInGui(guiGraphics, fluidVariant, slot.x, slot.y, fluidSlot.getAmount() == 0);
            // draw fluid count
            if (fluidSlot.getAmount() > 0)
                FluidRenderer.drawFluidCount(font, guiGraphics, fluidSlot.getAmount(), slot.x, slot.y);
        }
        if (fluidSlot.isLocked()) {
            // locked dither outline
            guiGraphics.blit(WIDGETS_TEXTURE, fluidSlot.x, fluidSlot.y, 0, 46, 16, 16);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics context, int x, int y) {
        if (this.hoveredSlot instanceof FluidSlot fluidSlot) {
            boolean hasCursorStack = !this.menu.getCarried().isEmpty();
            boolean shouldAddFluidSlotInfo = fluidSlot.getAmount() > 0 || fluidSlot.isLocked();
            if (!shouldAddFluidSlotInfo && !hasCursorStack)
                return;

            FluidStack fluidVariant = fluidSlot.getFluid();
            List<Component> tooltip = new ArrayList<>();
            if (shouldAddFluidSlotInfo)
                tooltip.addAll(FluidHelper.getTooltipForFluidStorage(fluidVariant, fluidSlot.getAmount(),
                        fluidSlot.getCapacity()));

            if (hasCursorStack)
                tooltip.add(Texts.FLUIDSLOT_HOVER_TOOLTIP);

            context.renderComponentTooltip(font, tooltip, x, y);
            return;
        }
        super.renderTooltip(context, x, y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.hoveredSlot instanceof FluidSlot fluidSlot && button == 0 && this.isLockSlotKeyDown) {
            this.menu.handleSlotLock(fluidSlot, this.menu.getCarried());

            this.skipNextRelease = true;
            return true;

        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (TankStorageClient.lockSlotKeyBinding.matches(keyCode, scanCode))
            this.isLockSlotKeyDown = true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (TankStorageClient.lockSlotKeyBinding.matches(keyCode, scanCode))
            this.isLockSlotKeyDown = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
