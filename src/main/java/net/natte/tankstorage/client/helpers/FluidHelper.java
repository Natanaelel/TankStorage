/*
 * Code taken from Modern Industrialization
 * https://github.com/AztechMC/Modern-Industrialization/blob/1.20.1/src/main/java/aztech/modern_industrialization/util/FluidHelper.java
 * Thanks!
 */

package net.natte.tankstorage.client.helpers;


import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class FluidHelper {
    static final int BUCKET = 1000;

    public static Component getFluidAmount(long amount, long capacity) {
        if (capacity < 100 * BUCKET || Util.isShiftDown.get()) {
            String text = FluidTextHelper.getUnicodeMillibuckets(amount, false) + " / " + capacity / 81;
            return Component.literal(text + " mB");
        } else {
            var maxedAmount = TextHelper.getMaxedAmount((double) amount / BUCKET,
                    (double) capacity / BUCKET);
            return Component.literal(maxedAmount.digit() + " / " + maxedAmount.maxDigit() + " " + maxedAmount.unit() + "B");
        }
    }

    public static List<Component> getTooltip(FluidStack fluid) {
        if (fluid.isEmpty()) {
            return new ArrayList<>();
        }
        return getTooltip(fluid, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }

    public static List<Component> getTooltipForFluidStorage(FluidStack fluid, long amount, long capacity) {
        List<Component> tooltip = getTooltip(fluid);
        tooltip.add(getFluidAmount(amount, capacity));
        return tooltip;
    }

    public static List<Component> getTooltip(FluidStack fluidVariant, TooltipFlag context) {
        List<Component> tooltip = new ArrayList<>();

        // Name first
        tooltip.add(getName(fluidVariant));

        // If advanced tooltips are enabled, render the fluid id
        if (context.isAdvanced()) {
            tooltip.add(Component.literal(BuiltInRegistries.FLUID.getKey(fluidVariant.getFluid()).toString()).withStyle(ChatFormatting.DARK_GRAY));
        }

        // TODO: consider adding an event to append to tooltips?

        return tooltip;
    }

    public static Component getName(FluidStack variant) {
        return variant.getFluid().getFluidType().getDescription(variant);
    }
}
