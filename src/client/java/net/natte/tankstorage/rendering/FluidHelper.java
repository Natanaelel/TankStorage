package net.natte.tankstorage.rendering;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.natte.tankstorage.util.Util;

public class FluidHelper {
    public static Text getFluidName(FluidVariant fluid, boolean grayIfEmpty) {
        if (fluid.isBlank()) {
            Style style = grayIfEmpty ? Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(false) : Style.EMPTY;
            return Text.translatable("tooltip.tankstorage.emptyfluid").setStyle(style);
        } else {
            return FluidVariantAttributes.getName(fluid);
        }
    }

    public static Text getFluidAmount(long amount, long capacity) {
        if (capacity < 100 * FluidConstants.BUCKET || Util.isShiftDown.get()) {
            String text = FluidTextHelper.getUnicodeMillibuckets(amount, false) + " / " + capacity / 81;
            return Text.literal(text + " mB");
        } else {
            var maxedAmount = TextHelper.getMaxedAmount((double) amount / FluidConstants.BUCKET,
                    (double) capacity / FluidConstants.BUCKET);
            return Text.literal(maxedAmount.digit() + " / " + maxedAmount.maxDigit() + " " + maxedAmount.unit() + "B");
        }

    }

    public static Text getFluidAmount(long amount) {
        if (amount < 100 * FluidConstants.BUCKET || Util.isShiftDown.get()) {
            String text = FluidTextHelper.getUnicodeMillibuckets(amount, false);
            return Text.literal(text + " mB");
        } else {
            return getFluidAmountLarge(amount);
        }
    }

    public static Text getFluidAmountLarge(long amount) {
        var amountUnit = TextHelper.getAmount((double) amount / FluidConstants.BUCKET);
        return Text.literal(amountUnit.digit() + " " + amountUnit.unit() + "B");
    }

    public static int getColorMinLuminance(int color) {
        int r = (color & 0xFF);
        int g = (color & 0xFF00) >> 8;
        int b = (color & 0xFF0000) >> 16;
        double lum = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255d;
        if (lum < 0.3) {
            if (lum == 0) {
                return 0x4C4C4C;
            } else {
                r = Math.min((int) (r * 0.3 / lum), 255);
                g = Math.min((int) (g * 0.3 / lum), 255);
                b = Math.min((int) (b * 0.3 / lum), 255);
                return r + (g << 8) + (b << 16);
            }
        } else {
            return color;
        }
    }

    public static List<Text> getTooltip(FluidVariant fluid, boolean grayIfEmpty) {

        if (fluid.isBlank()) {
            ArrayList<Text> list = new ArrayList<>();
            // list.add(getFluidName(fluid, grayIfEmpty));
            return list;
        }
        return FluidVariantRendering.getTooltip(fluid);
    }

    public static List<Text> getTooltipForFluidStorage(FluidVariant fluid, long amount, long capacity, boolean grayIfEmpty) {
        List<Text> tooltip = getTooltip(fluid, grayIfEmpty);
        tooltip.add(getFluidAmount(amount, capacity));
        return tooltip;
    }

    public static List<Text> getTooltipForFluidStorage(FluidVariant fluid, long amount, long capacity) {
        return getTooltipForFluidStorage(fluid, amount, capacity, true);
    }

    
}