package net.natte.tankstorage.storage;

import net.natte.tankstorage.util.FluidSlotData;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class TankSingleFluidStorage {

    private final int capacity;
    private int amount;
    // don't care about the FluidStack count, as long as it is >= 1
    private FluidStack fluidVariant;
    private boolean isLocked;

    private Runnable onMarkDirty;

    public TankSingleFluidStorage(int capacity, int amount, FluidStack fluidVariant, boolean isLocked) {
        this.capacity = capacity;
        this.amount = amount;
        this.fluidVariant = fluidVariant;
        this.isLocked = isLocked;
    }

    public TankSingleFluidStorage(int capacity) {
        this(capacity, 0, FluidStack.EMPTY, false);
    }

    public void setMarkDirtyListener(Runnable listener) {
        this.onMarkDirty = listener;
    }

    public TankSingleFluidStorage update(int amount, FluidStack fluidVariant, boolean isLocked) {
        this.amount = amount;
        this.fluidVariant = fluidVariant;
        this.isLocked = isLocked;
        return this;
    }

    /**
     * @return how much was inserted
     */
    public int insert(FluidStack insertedVariant, int maxAmount, boolean simulate) {
        if (!canInsert(insertedVariant))
            return 0;

        int space = capacity - amount;
        int insertedAmount = Math.min(maxAmount, space);
        if (insertedAmount > 0 && !simulate) {
            this.amount += insertedAmount;
            if (this.fluidVariant.isEmpty())
                this.fluidVariant = insertedVariant.copyWithAmount(1);
            markDirty();
        }


        return insertedAmount;
    }

    /**
     * @return how much was extracted
     */
    public int extract(FluidStack extractedVariant, int maxAmount, boolean simulate) {
        if (!canExtract(extractedVariant))
            return 0;

        int extractedAmount = Math.min(maxAmount, this.amount);
        if (extractedAmount > 0 && !simulate) {
            this.amount -= extractedAmount;
            if (this.amount == 0 && !this.isLocked)
                this.fluidVariant = FluidStack.EMPTY;
            markDirty();
        }

        return extractedAmount;
    }

    public boolean canInsert(FluidStack insertedVariant) {
        if (insertedVariant.isEmpty())
            return false;

        if (this.fluidVariant.isEmpty())
            return !this.isLocked;

        return FluidStack.isSameFluidSameComponents(this.fluidVariant, insertedVariant);
    }

    private boolean canExtract(FluidStack extractedVariant) {
        if (extractedVariant.isEmpty())
            return false;
        return FluidStack.isSameFluidSameComponents(this.fluidVariant, extractedVariant);
    }

    public FluidStack getFluid() {
        return fluidVariant;
    }

    public int getAmount() {
        return amount;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isLocked() {
        return isLocked;
    }

    private void markDirty() {
        if (this.onMarkDirty != null) {
            this.onMarkDirty.run();
        }
    }

    public void lock(FluidStack newFluidVariant, boolean shouldLock) {
        if (shouldLock) {
            if (this.amount == 0) {
                this.fluidVariant = newFluidVariant.copyWithAmount(1);
                this.isLocked = true;
            } else if (FluidStack.isSameFluid(this.fluidVariant, newFluidVariant)) {
                this.isLocked = true;
            }
        } else {
            this.isLocked = false;
            if (this.amount == 0)
                this.fluidVariant = FluidStack.EMPTY;
        }
    }

    public static TankSingleFluidStorage from(FluidSlotData fluidSlotData) {
        return new TankSingleFluidStorage(fluidSlotData.capacity(), fluidSlotData.amount(),
                fluidSlotData.fluidVariant(), fluidSlotData.isLocked());
    }

    public IFluidHandler getFluidHandler() {
        return new SlotFluidHandler(this);
    }
}
