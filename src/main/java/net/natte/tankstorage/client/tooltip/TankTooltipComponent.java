package net.natte.tankstorage.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.natte.tankstorage.client.rendering.FluidRenderer;
import net.natte.tankstorage.item.tooltip.TankTooltipData;
import net.natte.tankstorage.util.LargeFluidSlotData;
import net.natte.tankstorage.util.Util;

import java.util.List;

public class TankTooltipComponent implements ClientTooltipComponent {

    public static final ResourceLocation TEXTURE = Util.ID("textures/gui/widgets.png");

    private final List<LargeFluidSlotData> fluids;
    private final int selectedSlot;

    // renders selected bucket before other slots if bucket is selected
    public TankTooltipComponent(TankTooltipData tooltipData) {
        this.fluids = tooltipData.fluids();
        this.selectedSlot = tooltipData.selectedSlot();
    }
    
    @Override
    public int getWidth(Font textRenderer) {
        return getColumns() * 18 + 2;
    }

    @Override
    public int getHeight() {
        return getRows() * 18 + 2 + 4;
    }

    private int getRows() {
        return Math.max(1, Mth.ceil(fluids.size() / 9d));
    }

    private int getColumns() {
        return Math.min(9, fluids.size());
    }

    @Override
    public void renderImage(Font textRenderer, int x, int y, GuiGraphics context) {
        drawBackground(x, y, context);
        drawFluids(textRenderer, x, y, context);
        if (selectedSlot != -2)
            drawSlotHighlight(x, y, context);
    }

    private void drawBackground(int x, int y, GuiGraphics context) {
        int row = 0;
        int col = 0;
        for (int i = selectedSlot == -1 ? -1 : 0; i < fluids.size(); ++i) {
            context.blit(TEXTURE, x + col * 18, y + row * 18, 20, 128, 20, 20);
            ++col;
            if (col == 9) {
                col = 0;
                ++row;
            }
        }
    }

    private void drawFluids(Font textRenderer, int x, int y, GuiGraphics context) {
        int row = 0;
        int col = 0;
        if (selectedSlot == -1) {
            // slot texture
            context.blit(TEXTURE, x + 1, y + 1, 1, 129, 18, 18);
            context.renderItem(Items.BUCKET.getDefaultInstance(), x + 2, y + 2);
            ++col;
        }
        for (LargeFluidSlotData fluid : fluids) {
            drawFluidInSlot(fluid, context, textRenderer, x + col * 18 + 1, y + row * 18 + 1);
            ++col;
            if (col == 9) {
                col = 0;
                ++row;
            }
        }
    }

    private void drawFluidInSlot(LargeFluidSlotData fluid, GuiGraphics context, Font textRenderer, int x, int y) {
        // slot texture
        context.blit(TEXTURE, x, y, 1, 129, 18, 18);
        // fluid
        FluidRenderer.drawFluidInGui(context, fluid.fluid(), x + 1, y + 1, false);
        // fluid count
        FluidRenderer.drawFluidCount(textRenderer, context, fluid.amount(), x + 1, y + 1);
    }

    private void drawSlotHighlight(int x, int y, GuiGraphics context) {
        int slotIndex = selectedSlot == -1 ? 0 : selectedSlot;
        int xOffset = x + 18 * (slotIndex % 9) - 1;
        int yOffset = y + 18 * (slotIndex / 9) - 1;
        // highlight texture
        context.blit(TEXTURE, xOffset, yOffset, 25, 23, 22, 22);
    }
}
