package net.natte.tankstorage.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.natte.tankstorage.screenhandler.TankScreenHandler;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LockSlotPacketC2S(int syncId, int slot) implements CustomPacketPayload {

    public static final Type<LockSlotPacketC2S> TYPE = new Type<>(Util.ID("lock_slot_c2s"));
    public static final StreamCodec<ByteBuf, LockSlotPacketC2S> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            LockSlotPacketC2S::syncId,
            ByteBufCodecs.INT,
            LockSlotPacketC2S::slot,
            LockSlotPacketC2S::new
    );

    @Override
    public Type<LockSlotPacketC2S> type() {
        return TYPE;
    }

    public static void receive(LockSlotPacketC2S packet, IPayloadContext context) {
        if (packet.syncId == context.player().containerMenu.containerId
                && context.player().containerMenu instanceof TankScreenHandler tankScreenHandler) {
            tankScreenHandler.lockSlotClick(packet.slot);
        }
    }
}
