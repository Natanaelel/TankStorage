package net.natte.tankstorage.cache;

import net.natte.tankstorage.storage.InsertMode;
import net.natte.tankstorage.storage.TankFluidHandler;
import net.natte.tankstorage.storage.TankSingleFluidStorage;
import net.natte.tankstorage.util.FluidSlotData;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.*;

public class CachedFluidStorageState {

    @SuppressWarnings("unused")
    private UUID uuid;
    private int revision;
    private List<FluidSlotData> fluids;
    private List<FluidSlotData> uniqueFluids;

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

    public TankFluidHandler getFluidHandler(InsertMode insertMode) {
        if (this.parts == null) {
            this.parts = fluids.stream().map(TankSingleFluidStorage::from).toList();
        }
        // don't set mark dirty listener because client should never change contents
        // without the server knowing and doing it too
        return new TankFluidHandler(parts, insertMode);
    }

    // TODO: large FluidSlotData with long amount
    public List<FluidSlotData> getUniqueFluids() {
        if (uniqueFluids == null) {
            Map<FluidStack, Long> counts = new LinkedHashMap<>();
            for (FluidSlotData fluidSlotData : fluids) {
                long count = counts.getOrDefault(fluidSlotData.fluidVariant(), 0L);
                count += fluidSlotData.amount();
                counts.put(fluidSlotData.fluidVariant(), count);
            }
            uniqueFluids = new ArrayList<>();
            counts.forEach((fluidVariant, count) -> {
                if (count > 0)
//                    uniqueFluids.add(new FluidSlotData(fluidVariant, 0L, count, false));
                    uniqueFluids.add(new FluidSlotData(fluidVariant, 0, count.intValue(), false));
            });

        }

        return uniqueFluids;
    }
}
