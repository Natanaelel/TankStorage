package net.natte.tankstorage.storage;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

public class TankSingleFluidStorage extends SnapshotParticipant<FluidSlotData> implements SingleSlotStorage<FluidVariant> {

    private long capacity;
    private long amount;
    private FluidVariant fluidVariant;
    private boolean isLocked;
    private boolean isDirty = false;

    public TankSingleFluidStorage(long capacity, long amount, FluidVariant fluidVariant, boolean isLocked) {
        this.capacity = capacity;
        this.amount = amount;
        this.fluidVariant = fluidVariant;
        this.isLocked = isLocked;
    }

    public TankSingleFluidStorage(long capacity) {
        this(capacity, 0, FluidVariant.blank(), false);
    }

    public TankSingleFluidStorage update(long amount, FluidVariant fluidVariant, boolean isLocked) {
        this.amount = amount;
        this.fluidVariant = fluidVariant;
        this.isLocked = isLocked;
        return this;
    }

    @Override
    public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        if (!canInsert(insertedVariant))
            return 0;

        updateSnapshots(transaction);

        long space = capacity - amount;
        long insertedAmount = Math.min(maxAmount, space);

        if (this.fluidVariant.isBlank())
            this.fluidVariant = insertedVariant;

        this.amount += insertedAmount;

        return insertedAmount;
    }

    @Override
    public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction) {
        if (!canExtract(extractedVariant))
            return 0;

        updateSnapshots(transaction);

        long extractedAmount = Math.min(maxAmount, this.amount);

        this.amount -= extractedAmount;
        if (this.amount == 0 && !this.isLocked)
            this.fluidVariant = FluidVariant.blank();

        return extractedAmount;

    }

    public boolean canInsert(FluidVariant insertedVariant) {
        if (insertedVariant.equals(this.fluidVariant))
            return true;
        if (this.fluidVariant.isBlank() && !this.isLocked)
            return true;
        return false;
    }

    private boolean canExtract(FluidVariant extractedVariant) {
        if (extractedVariant.equals(this.fluidVariant))
            return true;
        return false;
    }

    @Override
    public boolean isResourceBlank() {
        return fluidVariant.isBlank();
    }

    @Override
    public FluidVariant getResource() {
        return fluidVariant;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    public boolean isLocked() {
        return isLocked;
    }

    @Override
    protected FluidSlotData createSnapshot() {
        return new FluidSlotData(fluidVariant, capacity, amount, isLocked);
    }

    @Override
    protected void readSnapshot(FluidSlotData snapshot) {
        this.fluidVariant = snapshot.fluidVariant();
        this.capacity = snapshot.capacity();
        this.amount = snapshot.amount();
        this.isLocked = snapshot.isLocked();
    }

    @Override
    protected void onFinalCommit() {
        markDirty();
    }

    private void markDirty() {
        this.isDirty = true;
    }

}
