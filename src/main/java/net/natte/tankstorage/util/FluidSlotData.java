package net.natte.tankstorage.util;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.network.PacketByteBuf;
import net.natte.tankstorage.storage.TankSingleFluidStorage;

public record FluidSlotData(FluidVariant fluidVariant, long amount, boolean isLocked) {

    public static FluidSlotData read(PacketByteBuf buf) {
        return new FluidSlotData(FluidVariant.fromPacket(buf), buf.readLong(), buf.readBoolean());
    }

    public void write(PacketByteBuf buf) {
        fluidVariant.toPacket(buf);
        buf.writeLong(amount);
        buf.writeBoolean(isLocked);
    }

    public boolean equalsOther(TankSingleFluidStorage other){
        return isLocked == other.isLocked() && amount == other.getAmount() && fluidVariant.equals(other.getResource());
    }

}
