/*
 * Code taken from Modern Industrialization
 * https://github.com/AztechMC/Modern-Industrialization/blob/1.20.1/src/main/java/aztech/modern_industrialization/util/FluidHelper.java
 * Thanks!
 */

package net.natte.tankstorage.rendering;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.text.Text;
import net.natte.tankstorage.util.Util;

public class FluidHelper {

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

    public static List<Text> getTooltip(FluidVariant fluid) {

        if (fluid.isBlank()) {
            ArrayList<Text> list = new ArrayList<>();
            return list;
        }
        return FluidVariantRendering.getTooltip(fluid);
    }

    public static List<Text> getTooltipForFluidStorage(FluidVariant fluid, long amount, long capacity) {
        List<Text> tooltip = getTooltip(fluid);
        tooltip.add(getFluidAmount(amount, capacity));
        return tooltip;
    }

}