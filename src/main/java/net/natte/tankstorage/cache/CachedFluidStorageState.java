package net.natte.tankstorage.cache;

import java.util.List;
import java.util.UUID;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.natte.tankstorage.storage.InsertMode;
import net.natte.tankstorage.storage.TankFluidStorage;
import net.natte.tankstorage.storage.TankSingleFluidStorage;
import net.natte.tankstorage.util.FluidSlotData;

public class CachedFluidStorageState {

    @SuppressWarnings("unused")
    private UUID uuid;
    private int revision;
    private List<FluidSlotData> fluids;

    private List<TankSingleFluidStorage> parts;

    public CachedFluidStorageState(UUID uuid, List<FluidSlotData> fluids, int revision) {
        this.uuid = uuid;
        this.fluids = fluids;
        this.revision = revision;
    }

    public int getRevision() {
        return revision;
    }

    public List<FluidSlotData> getFluids() {
        return fluids;
    }

    public Storage<FluidVariant> getFluidStorage(InsertMode insertMode) {
        if (this.parts == null) {
            this.parts = fluids.stream().map(TankSingleFluidStorage::from).toList();
        }
        // don't set mark dirty listener because client should never change contents
        // without the server knowing and doing it too
        return new TankFluidStorage(parts, insertMode);
    }
}
