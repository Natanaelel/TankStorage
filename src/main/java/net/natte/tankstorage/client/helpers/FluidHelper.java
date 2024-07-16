/*
 * Code taken from Modern Industrialization
 * https://github.com/AztechMC/Modern-Industrialization/blob/1.20.1/src/main/java/aztech/modern_industrialization/util/FluidHelper.java
 * Thanks!
 */

package net.natte.tankstorage.client.helpers;


import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.List;

public class FluidHelper {
    static final int BUCKET = FluidType.BUCKET_VOLUME;

    public static List<Component> appendTooltipForFluidStorage(List<Component> tooltip, FluidStack fluidStack, long amount, long capacity) {
        if (!fluidStack.isEmpty()) {
            tooltip.add(fluidStack.getFluidType().getDescription(fluidStack));

            if (Minecraft.getInstance().options.advancedItemTooltips)
                tooltip.add(Component.literal(BuiltInRegistries.FLUID.getKey(fluidStack.getFluid()).toString()).withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(getFluidAmount(amount, capacity));
        return tooltip;
    }

    private static Component getFluidAmount(long amount, long capacity) {
        if (capacity < 100 * BUCKET || Screen.hasShiftDown()) {
            return Component.literal(amount + " / " + capacity + " mB");
        } else {
            var maxedAmount = TextHelper.getMaxedAmount((double) amount / BUCKET,
                    (double) capacity / BUCKET);
            return Component.literal(maxedAmount.digit() + " / " + maxedAmount.maxDigit() + " " + maxedAmount.unit() + "B");
        }
    }
}
