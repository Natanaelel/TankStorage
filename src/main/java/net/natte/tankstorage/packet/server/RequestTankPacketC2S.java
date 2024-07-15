package net.natte.tankstorage.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.natte.tankstorage.packet.client.TankPacketS2C;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record RequestTankPacketC2S(UUID uuid, int revision) implements CustomPacketPayload {

    public static final Type<RequestTankPacketC2S> TYPE = new Type<>(Util.ID("request_tank_c2s"));
    public static final StreamCodec<ByteBuf, RequestTankPacketC2S> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            RequestTankPacketC2S::uuid,
            ByteBufCodecs.INT,
            RequestTankPacketC2S::revision,
            RequestTankPacketC2S::new
    );

    @Override
    public Type<RequestTankPacketC2S> type() {
        return TYPE;
    }

    public static void receive(RequestTankPacketC2S packet, IPayloadContext context) {
        TankFluidStorageState tank = Util.getFluidStorage(packet.uuid);
        if (tank != null && tank.getRevision() != packet.revision) {
            context.reply(new TankPacketS2C(tank.uuid, tank.getRevision(), tank.getFluidSlots()));
        }
    }
}
