package net.natte.tankstorage.tooltip;

import java.util.List;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.natte.tankstorage.item.tooltip.TankTooltipData;
import net.natte.tankstorage.rendering.FluidRenderer;
import net.natte.tankstorage.util.FluidSlotData;
import net.natte.tankstorage.util.Util;

public class TankTooltipComponent implements TooltipComponent {

    public static final Identifier TEXTURE = Util.ID("/textures/gui/widgets.png");

    private final List<FluidSlotData> fluids;
    private final int selectedSlot;

    // renders selected bucket before other slots if bucket is selected
    public TankTooltipComponent(TankTooltipData tooltipData) {
        this.fluids = tooltipData.fluids();
        this.selectedSlot = tooltipData.selectedSlot();
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return getColumns() * 18 + 2;
    }

    @Override
    public int getHeight() {
        return getRows() * 18 + 2 + 4;
    }

    private int getRows() {
        return MathHelper.ceil(fluids.size() / 9d);
    }

    private int getColumns() {
        return Math.min(9, fluids.size());
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        drawBackground(x, y, context);
        drawFluids(textRenderer, x, y, context);
        if (selectedSlot != -2)
            drawSlotHighlight(x, y, context);
    }

    private void drawBackground(int x, int y, DrawContext context) {
        int row = 0;
        int col = 0;
        for (int i = selectedSlot == -1 ? -1 : 0; i < fluids.size(); ++i) {
            context.drawTexture(TEXTURE, x + col * 18, y + row * 18, 20, 128, 20, 20);
            ++col;
            if (col == 9) {
                col = 0;
                ++row;
            }
        }
    }

    private void drawFluids(TextRenderer textRenderer, int x, int y, DrawContext context) {
        int row = 0;
        int col = 0;
        if (selectedSlot == -1) {
            // slot texture
            context.drawTexture(TEXTURE, x + 1, y + 1, 1, 129, 18, 18);
            context.drawItem(Items.BUCKET.getDefaultStack(), x + col * 18 + 2, y + row * 18 + 2);
            ++col;
        }
        for (FluidSlotData fluid : fluids) {
            drawFluidInSlot(fluid, context, textRenderer, x + col * 18 + 1, y + row * 18 + 1);
            ++col;
            if (col == 9) {
                col = 0;
                ++row;
            }
        }
    }

    private void drawFluidInSlot(FluidSlotData fluid, DrawContext context, TextRenderer textRenderer, int x, int y) {
        // slot texture
        context.drawTexture(TEXTURE, x, y, 1, 129, 18, 18);
        // fluid
        FluidRenderer.drawFluidInGui(context, fluid.fluidVariant(), x + 1, y + 1);
        // fluid count
        FluidRenderer.drawFluidCount(textRenderer, context, fluid.amount(), x + 1, y + 1);
    }

    private void drawSlotHighlight(int x, int y, DrawContext context) {
        int slotIndex = selectedSlot == -1 ? 0 : selectedSlot;
        int xOffset = x + 18 * (slotIndex % 9) - 1;
        int yOffset = y + 18 * (slotIndex / 9) - 1;
        // highlight texture
        context.drawTexture(TEXTURE, xOffset, yOffset, 25, 23, 22, 22);
    }
}
