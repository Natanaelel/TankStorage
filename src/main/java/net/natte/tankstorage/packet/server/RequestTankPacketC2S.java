package net.natte.tankstorage.packet.server;

import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.tankstorage.packet.client.TankPacketS2C;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.util.Util;

public record RequestTankPacketC2S(UUID uuid, int revision) implements FabricPacket {

    public static final PacketType<RequestTankPacketC2S> PACKET_TYPE = PacketType.create(Util.ID("request_tank_c2s"),
            RequestTankPacketC2S::read);

    public static RequestTankPacketC2S read(PacketByteBuf buf) {
        return new RequestTankPacketC2S(buf.readUuid(), buf.readInt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(uuid);
        buf.writeInt(revision);
    }

    @Override
    public PacketType<RequestTankPacketC2S> getType() {
        return PACKET_TYPE;
    }

    public static void receive(RequestTankPacketC2S packet, ServerPlayerEntity player, PacketSender responseSender) {
        TankFluidStorageState tank = Util.getFluidStorage(packet.uuid, player.getWorld());
        if (tank.getRevision() != packet.revision) {
            responseSender.sendPacket(new TankPacketS2C(tank.uuid, tank.getRevision(), tank.getFluidSlotDatas()));
        }
    }
}
