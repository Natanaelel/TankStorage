package net.natte.tankstorage.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.natte.tankstorage.sync.SyncSubscriptionManager;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record SyncSubscribePacketC2S(UUID tankUuid, boolean subscribe) implements CustomPacketPayload {

    public static final Type<SyncSubscribePacketC2S> TYPE = new Type<>(Util.ID("sync_subscribe_c2s"));
    public static final StreamCodec<ByteBuf, SyncSubscribePacketC2S> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            SyncSubscribePacketC2S::tankUuid,
            ByteBufCodecs.BOOL,
            SyncSubscribePacketC2S::subscribe,
            SyncSubscribePacketC2S::new
    );

    public static CustomPacketPayload subscribe(UUID uuid) {
        return new SyncSubscribePacketC2S(uuid, true);
    }

    public static CustomPacketPayload unsubscribe(UUID uuid) {
        return new SyncSubscribePacketC2S(uuid, false);
    }

    @Override
    public Type<SyncSubscribePacketC2S> type() {
        return TYPE;
    }

    public static void receive(SyncSubscribePacketC2S packet, IPayloadContext context) {
        if (packet.subscribe)
            SyncSubscriptionManager.subscribe(context.player().getUUID(), packet.tankUuid);
        else
            SyncSubscriptionManager.unsubscribe(context.player().getUUID(), packet.tankUuid);
    }
}
