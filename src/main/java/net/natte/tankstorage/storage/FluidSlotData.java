package net.natte.tankstorage.storage;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

public record FluidSlotData(FluidVariant fluidVariant, long capacity, long amount, boolean isLocked) {
}
