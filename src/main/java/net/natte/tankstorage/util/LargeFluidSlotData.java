package net.natte.tankstorage.util;

import net.neoforged.neoforge.fluids.FluidStack;

public record LargeFluidSlotData(FluidStack fluid, long capacity, long amount, boolean isLocked) {
    
}
