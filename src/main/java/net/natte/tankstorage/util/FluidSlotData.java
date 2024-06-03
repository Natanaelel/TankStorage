package net.natte.tankstorage.util;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.network.PacketByteBuf;
import net.natte.tankstorage.storage.TankSingleFluidStorage;

public record FluidSlotData(FluidVariant fluidVariant, long capacity, long amount, boolean isLocked) {

    public static FluidSlotData read(PacketByteBuf buf) {
        return new FluidSlotData(FluidVariant.fromPacket(buf), buf.readLong(), buf.readLong(), buf.readBoolean());
    }

    public void write(PacketByteBuf buf) {
        fluidVariant.toPacket(buf);
        buf.writeLong(capacity);
        buf.writeLong(amount);
        buf.writeBoolean(isLocked);
    }

    public boolean equalsOther(TankSingleFluidStorage other) {
        return isLocked == other.isLocked() && capacity == other.getCapacity() && amount == other.getAmount()
                && fluidVariant.equals(other.getResource());
    }

    public static FluidSlotData from(TankSingleFluidStorage fluidStorage) {
        return new FluidSlotData(fluidStorage.getResource(), fluidStorage.getCapacity(),
                fluidStorage.getAmount(), fluidStorage.isLocked());
    }

}
