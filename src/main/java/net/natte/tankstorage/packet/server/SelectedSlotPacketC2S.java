package net.natte.tankstorage.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectedSlotPacketC2S(boolean isRight, int slot) implements CustomPacketPayload {
    public static final Type<SelectedSlotPacketC2S> TYPE = new Type<>(Util.ID("selected_slot_c2s"));
    public static final StreamCodec<ByteBuf, SelectedSlotPacketC2S> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            SelectedSlotPacketC2S::isRight,
            ByteBufCodecs.INT,
            SelectedSlotPacketC2S::slot,
            SelectedSlotPacketC2S::new
    );

    @Override
    public Type<SelectedSlotPacketC2S> type() {
        return TYPE;
    }

    public static void receive(SelectedSlotPacketC2S packet, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        ItemStack stack = player.getItemInHand(packet.isRight ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        if (!Util.isTankLike(stack))
            return;
        if (!Util.hasUUID(stack))
            return;
        TankFluidStorageState tank = Util.getFluidStorage(Util.getUUID(stack));
        if (tank == null)
            return;
        int size = tank.getUniqueFluids().size();
        int selectedSlot = Mth.clamp(packet.slot, -1, size - 1);
        stack.set(TankStorage.SelectedSlotComponentType, selectedSlot);
    }
}
