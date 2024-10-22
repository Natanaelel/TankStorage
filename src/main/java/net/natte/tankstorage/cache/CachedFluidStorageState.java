package net.natte.tankstorage.cache;

import net.minecraft.util.Mth;
import net.natte.tankstorage.storage.InsertMode;
import net.natte.tankstorage.storage.TankFluidHandler;
import net.natte.tankstorage.storage.TankSingleFluidStorage;
import net.natte.tankstorage.util.FluidSlotData;
import net.natte.tankstorage.util.HashableFluidVariant;
import net.natte.tankstorage.util.LargeFluidSlotData;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CachedFluidStorageState {

    private final int revision;
    private final List<FluidSlotData> fluids;
    private List<LargeFluidSlotData> uniqueFluids;

    private List<TankSingleFluidStorage> parts;

    public CachedFluidStorageState(List<FluidSlotData> fluids, int revision) {
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

    public List<LargeFluidSlotData> getUniqueFluids() {
        if (uniqueFluids == null) {
            Map<HashableFluidVariant, Long> counts = new LinkedHashMap<>();
            for (FluidSlotData fluidSlotData : fluids)
                if(fluidSlotData.amount() > 0 || (fluidSlotData.isLocked() && !fluidSlotData.fluidVariant().isEmpty()))
                    counts.merge(new HashableFluidVariant(fluidSlotData.fluidVariant()), (long) fluidSlotData.amount(), Long::sum);

            uniqueFluids = new ArrayList<>();
            counts.forEach((fluidVariant, count) -> {
                uniqueFluids.add(new LargeFluidSlotData(fluidVariant.fluidStack(), 0L, count, false));
            });

        }

        return uniqueFluids;
    }

    @Nullable
    public FluidStack getSelectedFluid(int selectedSlot) {
        selectedSlot = Mth.clamp(selectedSlot, -1, getUniqueFluids().size() - 1);
        return selectedSlot == -1 ? null : getUniqueFluids().get(selectedSlot).fluid();
    }
}