package net.natte.tankstorage.util;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.natte.tankstorage.storage.TankSingleFluidStorage;
import net.neoforged.neoforge.fluids.FluidStack;

public record FluidSlotData(FluidStack fluidVariant, int capacity, int amount, boolean isLocked) {

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidSlotData> STREAM_CODEC = StreamCodec.composite(
            FluidStack.OPTIONAL_STREAM_CODEC,
            FluidSlotData::fluidVariant,
            ByteBufCodecs.INT,
            FluidSlotData::capacity,
            ByteBufCodecs.INT,
            FluidSlotData::amount,
            ByteBufCodecs.BOOL,
            FluidSlotData::isLocked,
            FluidSlotData::new
    );

    public FluidSlotData(FluidStack fluidVariant, int amount, boolean isLocked){
        this(fluidVariant, 0, amount, isLocked);
    }

    public boolean equalsOther(TankSingleFluidStorage other) {
        return isLocked == other.isLocked() && capacity == other.getCapacity() && amount == other.getAmount()
                && FluidStack.isSameFluidSameComponents(fluidVariant, other.getFluid());
    }

    public static FluidSlotData from(TankSingleFluidStorage fluidStorage) {
        return new FluidSlotData(fluidStorage.getFluid(), fluidStorage.getCapacity(),
                fluidStorage.getAmount(), fluidStorage.isLocked());
    }
}
