package net.natte.tankstorage.screen;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.natte.tankstorage.TankStorageClient;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.gui.FluidSlot;
import net.natte.tankstorage.packet.server.LockSlotPacketC2S;
import net.natte.tankstorage.rendering.FluidHelper;
import net.natte.tankstorage.rendering.FluidRenderer;
import net.natte.tankstorage.screenhandler.TankScreenHandler;
import net.natte.tankstorage.util.Util;

public class TankScreen extends HandledScreen<TankScreenHandler> {

    private static final Identifier WIDGETS_TEXTURE = Util.ID("textures/gui/widgets.png");

    private TankType type;
    private Identifier texture;

    public TankScreen(TankScreenHandler handler, PlayerInventory inventory, Text title, TankType type) {
        super(handler, inventory, title);
        this.type = type;

        this.texture = Util.ID("textures/gui/" + this.type.width() + "x" + this.type.height() + ".png");
        this.backgroundHeight = 114 + this.type.height() * 18;
        this.playerInventoryTitleY += this.type.height() * 18 - 52;
    }

    @Override
    protected void init() {
        super.init();

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext drawContext, float timeDelta, int mouseX, int mouseY) {

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawContext.drawTexture(this.texture, x, y, 0, 0, backgroundWidth, backgroundHeight,
                (int) Math.ceil(backgroundWidth / 256d) * 256, (int) Math.ceil(backgroundHeight / 256d) * 256);

    }

    @Override
    protected void drawSlot(DrawContext context, Slot slot) {
        if (!(slot instanceof FluidSlot fluidSlot)) {
            super.drawSlot(context, slot);
            return;
        }

        FluidVariant fluidVariant = fluidSlot.getFluidVariant();
        if (!fluidVariant.isBlank()) {
            FluidRenderer.drawFluidInGui(context, fluidVariant, slot.x, slot.y);
            // draw fluid count
            FluidRenderer.drawFluidCount(textRenderer, context, fluidSlot.getAmount(), slot.x, slot.y);
        }
        if (fluidSlot.isLocked()) {
            // locked dither outline
            context.drawTexture(WIDGETS_TEXTURE, fluidSlot.x, fluidSlot.y, 0, 46, 16, 16);
        }

    }

    @Override
    protected void drawMouseoverTooltip(DrawContext context, int x, int y) {
        if (this.focusedSlot instanceof FluidSlot fluidSlot) {

            if (fluidSlot.getAmount() == 0 && !fluidSlot.isLocked())
                return;

            FluidVariant fluidVariant = fluidSlot.getFluidVariant();
            List<Text> tooltip = new ArrayList<>(
                    FluidHelper.getTooltipForFluidStorage(fluidVariant, fluidSlot.getAmount(), fluidSlot.getCapacity()));

            tooltip.add(Text.translatable("tooltip.tankstorage.insert_or_extract_desc")
                    .setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
            context.drawTooltip(textRenderer, tooltip, x, y);
            return;
        }

        super.drawMouseoverTooltip(context, x, y);

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && TankStorageClient.lockSlotKeyBinding.isPressed()) {
            Slot slot = getSlotAt(mouseX, mouseY);
            if (slot != null) {
                int slotIndex = slot.id;
                if (slot instanceof FluidSlot) {
                    ClientPlayNetworking.send(new LockSlotPacketC2S(this.getScreenHandler().syncId, slotIndex));
                    this.cancelNextRelease = true;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (TankStorageClient.lockSlotKeyBinding.matchesKey(keyCode, scanCode))
            TankStorageClient.lockSlotKeyBinding.setPressed(true);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (TankStorageClient.lockSlotKeyBinding.matchesKey(keyCode, scanCode))
            TankStorageClient.lockSlotKeyBinding.setPressed(false);

        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
