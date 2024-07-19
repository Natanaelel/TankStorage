package net.natte.tankstorage.state;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.packet.client.TankPacketS2C;
import net.natte.tankstorage.storage.InsertMode;
import net.natte.tankstorage.storage.TankFluidHandler;
import net.natte.tankstorage.storage.TankSingleFluidStorage;
import net.natte.tankstorage.sync.SyncSubscriptionManager;
import net.natte.tankstorage.util.FluidSlotData;
import net.natte.tankstorage.util.HashableFluidVariant;
import net.natte.tankstorage.util.LargeFluidSlotData;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TankFluidStorageState {

    public TankType type;
    public UUID uuid;

    private List<TankSingleFluidStorage> fluidStorageParts;

    private short revision = 0; // start different from client (0) to update client cache
    private final List<Runnable> listeners = new ArrayList<>();
    private List<LargeFluidSlotData> uniqueFluids;

    public TankFluidStorageState(TankType type, UUID uuid, List<FluidSlotData> fluidSlots) {
        this(type, uuid);
        this.fluidStorageParts = new ArrayList<>();
        for (FluidSlotData slot : fluidSlots) {
            TankSingleFluidStorage fluidSlot = new TankSingleFluidStorage(this.type.getCapacity(), slot.amount(), slot.fluidVariant(), slot.isLocked());
            fluidSlot.setMarkDirtyListener(this::markDirty);
            this.fluidStorageParts.add(fluidSlot);
        }
    }

    private TankFluidStorageState(TankType type, UUID uuid) {
        this.type = type;
        this.uuid = uuid;
    }

    public void addOnChangeListener(Runnable listener) {
        this.listeners.add(listener);
    }

    public void removeOnChangeListener(Runnable listener) {
        this.listeners.remove(listener);
    }

//    // called only serverside
//    public TankFluidStorage getFluidStorage(InsertMode insertMode) {
//        TankFluidStorage fluidStorage = new TankFluidStorage(fluidStorageParts, insertMode);
//        fluidStorage.setMarkDirtyListener(this::markDirty);
//        // fluidStorage.
//        return fluidStorage;
//    }

    public TankFluidHandler getFluidHandler(InsertMode insertMode) {
        return new TankFluidHandler(fluidStorageParts, insertMode);
    }

    public TankFluidStorageState asType(TankType type) {
        if (this.type != type) {
            if (type.size() < this.type.size()) {
                return this;
            }
            return changeType(type);
        }
        return this;
    }

    public TankFluidStorageState changeType(TankType type) {
        TankStorage.LOGGER
                .debug("Upgrading tank from " + this.type.getName() + " to " + type.getName() + " uuid " + this.uuid);

        TankFluidStorageState tank = new TankFluidStorageState(type, this.uuid);
        tank.fluidStorageParts = new ArrayList<>();

        for (int i = 0; i < this.fluidStorageParts.size(); ++i) {
            TankSingleFluidStorage oldFluidStorage = this.fluidStorageParts.get(i);

            TankSingleFluidStorage newFluidSlot = new TankSingleFluidStorage(type.getCapacity(), oldFluidStorage.getAmount(),
                    oldFluidStorage.getFluid(), oldFluidStorage.isLocked());
            newFluidSlot.setMarkDirtyListener(tank::markDirty);
            tank.fluidStorageParts.add(newFluidSlot);
        }

        for (int i = this.fluidStorageParts.size(); i < type.size(); ++i) {
            TankSingleFluidStorage newFluidSlot = new TankSingleFluidStorage(type.getCapacity(), 0,
                    FluidStack.EMPTY, false);
            newFluidSlot.setMarkDirtyListener(tank::markDirty);
            tank.fluidStorageParts.add(newFluidSlot);
        }
        tank.markDirty();
        return tank;
    }


    public static TankFluidStorageState create(TankType type, UUID uuid) {
        TankFluidStorageState tank = new TankFluidStorageState(type, uuid);
        List<TankSingleFluidStorage> fluidStorageParts = new ArrayList<>(type.size());
        for (int i = 0; i < type.size(); ++i) {
            fluidStorageParts.add(new TankSingleFluidStorage(type.getCapacity()));
        }
        tank.fluidStorageParts = fluidStorageParts;
        return tank;
    }

    public TankSingleFluidStorage getPart(int slot) {
        return this.fluidStorageParts.get(slot);
    }

    public List<FluidSlotData> getFluidSlots() {
        List<FluidSlotData> fluids = new ArrayList<>();
        for (TankSingleFluidStorage part : fluidStorageParts) {
            fluids.add(new FluidSlotData(part.getFluid(), this.type.getCapacity(), part.getAmount(),
                    part.isLocked()));
        }
        return fluids;
    }

    public List<LargeFluidSlotData> getUniqueFluids() {
        if (this.uniqueFluids == null) {
            Map<HashableFluidVariant, Long> counts = new LinkedHashMap<>();
            for (TankSingleFluidStorage part : fluidStorageParts) {
                if (Util.canPlaceFluid(part.getFluid().getFluid()))
                    counts.merge(new HashableFluidVariant(part.getFluid()), ((long) part.getAmount()), Long::sum);
            }
            List<LargeFluidSlotData> uniqueFluids = new ArrayList<>();
            counts.forEach((fluidVariant, count) -> {
                if (count > 0)
                    uniqueFluids.add(new LargeFluidSlotData(fluidVariant.fluidStack(), 0L, count, false));
            });
            this.uniqueFluids = uniqueFluids;
        }
        return this.uniqueFluids;
    }

    public short getRevision() {
        return this.revision;
    }

    private void updateRevision() {
        this.revision = (short) ((this.revision + 1) & Short.MAX_VALUE);
    }

    // called only serverside
    public void markDirty() {
        this.uniqueFluids = null;
        this.updateRevision();
        for (Runnable listener : this.listeners)
            listener.run();
        SyncSubscriptionManager.setChanged(this.uuid);
    }

    // called only serverside
    public void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new TankPacketS2C(uuid, getRevision(), getFluidSlots()));
    }


    public TankType getType() {
        return type;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Nullable
    public FluidStack getSelectedFluid(int selectedSlot) {
        selectedSlot = Mth.clamp(selectedSlot, -1, getUniqueFluids().size() - 1);
        return selectedSlot == -1 ? null : getUniqueFluids().get(selectedSlot).fluid();
    }
}
