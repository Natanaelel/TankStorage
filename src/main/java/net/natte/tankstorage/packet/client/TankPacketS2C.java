package net.natte.tankstorage.packet.client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.natte.tankstorage.util.FluidSlotData;
import net.natte.tankstorage.util.Util;

public record TankPacketS2C(UUID uuid, int revision, List<FluidSlotData> fluids) implements FabricPacket {

    public static final PacketType<TankPacketS2C> PACKET_TYPE = PacketType.create(Util.ID("tank_s2c"),
            TankPacketS2C::read);

    public static TankPacketS2C read(PacketByteBuf buf) {
        UUID uuid = buf.readUuid();
        int revision = buf.readInt();
        int size = buf.readInt();
        List<FluidSlotData> fluids = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            fluids.add(FluidSlotData.read(buf));
        }
        return new TankPacketS2C(uuid, revision, fluids);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(uuid);
        buf.writeInt(revision);
        buf.writeInt(fluids.size());
        for (FluidSlotData fluidSlotData : fluids) {
            fluidSlotData.write(buf);
        }
    }

    @Override
    public PacketType<TankPacketS2C> getType() {
        return PACKET_TYPE;
    }

}
