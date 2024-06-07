package net.natte.tankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.tankstorage.screenhandler.TankScreenHandler;
import net.natte.tankstorage.util.Util;

public record LockSlotPacketC2S(int syncId, int slot)
        implements FabricPacket {

    public static final PacketType<LockSlotPacketC2S> PACKET_TYPE = PacketType.create(Util.ID("lock_slot_c2s"),
            LockSlotPacketC2S::read);

    public static LockSlotPacketC2S read(PacketByteBuf buf) {
        return new LockSlotPacketC2S(buf.readInt(), buf.readInt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(syncId);
        buf.writeInt(slot);
    }

    @Override
    public PacketType<LockSlotPacketC2S> getType() {
        return PACKET_TYPE;
    }

    public static void receive(LockSlotPacketC2S packet, ServerPlayerEntity player, PacketSender responseSender) {
        if (packet.syncId == player.currentScreenHandler.syncId
                && player.currentScreenHandler instanceof TankScreenHandler tankScreenHandler) {
            tankScreenHandler.lockSlotClick(packet.slot);
        }
    }
}
