package net.natte.tankstorage.cache;

import java.util.List;
import java.util.UUID;

import net.natte.tankstorage.util.FluidSlotData;

public class CachedFluidStorageState {

    private UUID uuid;
    private int revision;
    private List<FluidSlotData> fluids;

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

}
