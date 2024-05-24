package net.natte.tankstorage.screen;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.core.config.builder.api.Component;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.gui.FluidSlot;
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
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
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

        // fluid slot background. add in gui background texture?
        // context.drawTexture(WIDGETS_TEXTURE, fluidSlot.x-1, fluidSlot.y-1, 1, 149,
        // 18, 18);

        if (fluidSlot.isLocked()) {
            // locked dither outlike
            context.drawTexture(WIDGETS_TEXTURE, fluidSlot.x, fluidSlot.y, 0, 46, 16, 16);
        }
        FluidVariant fluidVariant = fluidSlot.getFluidVariant();
        // System.out.println(fluidVariant);
        if (!fluidVariant.isBlank()) {
            // System.out.println("drawfluid");
            FluidRenderer.drawFluidInGui(context, fluidVariant, slot.x, slot.y);
        } else {
            // FluidRenderer.drawFluidInGui(context, FluidVariant.of(Fluids.LAVA), slot.x,
            // slot.y);
        }

        // ishovering...?

    }

    @Override
    protected void drawMouseoverTooltip(DrawContext context, int x, int y) {
        if(this.focusedSlot instanceof FluidSlot fluidSlot){
            // long capacity = fluidSlot.getCapacity();
            // long amount = fluidSlot.getAmount();
            // Text text = Text.of((amount * 1000 / FluidConstants.BUCKET) + "/" + (capacity * 1000/ FluidConstants.BUCKET) + "mB");
            // context.drawTooltip(textRenderer, List.of(text), x, y);
            // return;


            FluidVariant fluidVariant = fluidSlot.getFluidVariant();
            List<Text> tooltip = new ArrayList<>(
                    FluidHelper.getTooltipForFluidStorage(fluidVariant, fluidSlot.getAmount(), fluidSlot.getCapacity(), false));

            tooltip.add(Text.translatable("tooltip.tankstorage.uh_insert_maebe").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
            context.drawTooltip(textRenderer, tooltip, x, y);
            return;
        }

        super.drawMouseoverTooltip(context, x, y);

    }
}
