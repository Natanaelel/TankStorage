package net.natte.tankstorage.packet.server;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.natte.tankstorage.gui.FluidSlot;
import net.natte.tankstorage.packet.screenHandler.SyncFluidPacketS2C;
import net.natte.tankstorage.menu.TankMenu;
import net.natte.tankstorage.util.FluidSlotData;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LockSlotPacketC2S(int syncId, int slot, FluidStack fluid,
                                boolean shouldLock) implements CustomPacketPayload {

    public static final Type<LockSlotPacketC2S> TYPE = new Type<>(Util.ID("lock_slot_c2s"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LockSlotPacketC2S> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            LockSlotPacketC2S::syncId,
            ByteBufCodecs.INT,
            LockSlotPacketC2S::slot,
            FluidStack.OPTIONAL_STREAM_CODEC,
            LockSlotPacketC2S::fluid,
            ByteBufCodecs.BOOL,
            LockSlotPacketC2S::shouldLock,
            LockSlotPacketC2S::new

    );

    @Override
    public Type<LockSlotPacketC2S> type() {
        return TYPE;
    }

    public static void receive(LockSlotPacketC2S packet, IPayloadContext context) {
        if (context.player().containerMenu instanceof TankMenu tankMenu && packet.syncId == tankMenu.containerId) {


            if (!tankMenu.lockSlot(packet.slot, packet.fluid, packet.shouldLock)) {
                // lock was invalid, update client to tell them to revert optimistic update
                if (packet.slot >= 0 && packet.slot < tankMenu.slots.size() && tankMenu.getSlot(packet.slot) instanceof FluidSlot fluidSlot) {
                    FluidSlotData fluidSlotData = new FluidSlotData(fluidSlot.getFluid(), fluidSlot.getCapacity(), fluidSlot.getAmount(), fluidSlot.isLocked());
                    context.reply(new SyncFluidPacketS2C(packet.syncId, packet.slot, fluidSlotData));
                }
            }
        }
    }
}
