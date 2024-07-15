package net.natte.tankstorage.packet.screenHandler;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.natte.tankstorage.screenhandler.TankMenu;
import net.natte.tankstorage.util.FluidSlotData;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncFluidPacketS2C(int syncId, int slot, FluidSlotData fluidSlotData) implements CustomPacketPayload {

    public static final Type<SyncFluidPacketS2C> TYPE = new Type<>(Util.ID("sync_fluid_s2c"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncFluidPacketS2C> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SyncFluidPacketS2C::syncId,
            ByteBufCodecs.INT,
            SyncFluidPacketS2C::slot,
            FluidSlotData.STREAM_CODEC,
            SyncFluidPacketS2C::fluidSlotData,
            SyncFluidPacketS2C::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void receive(SyncFluidPacketS2C packet, IPayloadContext context) {

        AbstractContainerMenu screenHandler = context.player().containerMenu;
        if (packet.syncId() != screenHandler.containerId)
            return;
        if (!(screenHandler instanceof TankMenu tankMenu))
            return;
        tankMenu.updateFluidSlot(packet.slot(), packet.fluidSlotData());
    }
}
