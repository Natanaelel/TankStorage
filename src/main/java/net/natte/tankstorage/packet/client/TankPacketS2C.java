package net.natte.tankstorage.packet.client;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.natte.tankstorage.cache.CachedFluidStorageState;
import net.natte.tankstorage.cache.ClientTankCache;
import net.natte.tankstorage.util.FluidSlotData;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record TankPacketS2C(UUID uuid, int revision, List<FluidSlotData> fluids) implements CustomPacketPayload {

    public static final Type<TankPacketS2C> TYPE = new Type<>(Util.ID("tank_s2c"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TankPacketS2C> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            TankPacketS2C::uuid,
            ByteBufCodecs.INT,
            TankPacketS2C::revision,
            FluidSlotData.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)),
            TankPacketS2C::fluids,
            TankPacketS2C::new
    );

    @Override
    public Type<TankPacketS2C> type() {
        return TYPE;
    }

    public static void receive(TankPacketS2C packet, IPayloadContext context) {
        ClientTankCache.put(packet.uuid,
                new CachedFluidStorageState(packet.uuid, packet.fluids, packet.revision));
    }
}
