package net.natte.tankstorage.packet.screenHandler;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.natte.tankstorage.util.FluidSlotData;
import net.natte.tankstorage.util.Util;

public record SyncFluidPacketS2C(int syncId, int slot, FluidSlotData fluidSlotData) implements FabricPacket {

    public static final PacketType<SyncFluidPacketS2C> PACKET_TYPE = PacketType.create(Util.ID("sync_fluid_s2c"),
            SyncFluidPacketS2C::read);

    public static SyncFluidPacketS2C read(PacketByteBuf buf) {
        return new SyncFluidPacketS2C(buf.readInt(), buf.readInt(), FluidSlotData.read(buf));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(syncId);
        buf.writeInt(slot);
        fluidSlotData.write(buf);
    }

    @Override
    public PacketType<SyncFluidPacketS2C> getType() {
        return PACKET_TYPE;
    }
}
