package net.natte.tankstorage.storage;

import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class TankFluidStorage implements Storage<FluidVariant> {

    private List<TankSingleFluidStorage> parts;
    private InsertMode insertMode;

    public TankFluidStorage(List<TankSingleFluidStorage> parts, InsertMode insertMode) {
        this.parts = parts;
        this.insertMode = insertMode;
    }

    @Override
    public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        long insertedAmount = 0;
        switch (insertMode) {
            case ALL:
                insertedAmount += insertIntoLockedSlots(insertedVariant, maxAmount - insertedAmount, transaction);
                insertedAmount += insertIntoNonEmptySlots(insertedVariant, maxAmount - insertedAmount, transaction);
                insertedAmount += insertIntoAnySlots(insertedVariant, maxAmount - insertedAmount, transaction);
                return insertedAmount;

            case FILTERED:
                insertedAmount += insertIntoLockedSlots(insertedVariant, maxAmount - insertedAmount, transaction);
                insertedAmount += insertIntoNonEmptySlots(insertedVariant, maxAmount - insertedAmount, transaction);
                if (hasSlotWithVariant(insertedVariant))
                    insertedAmount += insertIntoAnySlots(insertedVariant, maxAmount - insertedAmount, transaction);
                return insertedAmount;

            case VOID_OVERFLOW:
                insertedAmount += insertIntoLockedSlots(insertedVariant, maxAmount - insertedAmount, transaction);
                insertedAmount += insertIntoNonEmptySlots(insertedVariant, maxAmount - insertedAmount, transaction);
                insertedAmount = maxAmount;
                return insertedAmount;
            // >:( dumb java lsp
            default:
                return 0;
        }
    }

    private boolean hasSlotWithVariant(FluidVariant insertedVariant) {
        for (TankSingleFluidStorage part : parts) {
            if (part.getResource().equals(insertedVariant))
                return true;
        }
        return false;
    }

    private long insertIntoLockedSlots(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        long insertedAmount = 0;
        for (TankSingleFluidStorage part : parts) {
            if (insertedAmount == maxAmount)
                break;
            if (part.isLocked())
                insertedAmount += part.insert(insertedVariant, maxAmount - insertedAmount, transaction);
        }
        return insertedAmount;
    }

    private long insertIntoNonEmptySlots(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        long insertedAmount = 0;
        for (TankSingleFluidStorage part : parts) {
            if (insertedAmount == maxAmount)
                break;
            if (part.getAmount() > 0)
                insertedAmount += part.insert(insertedVariant, maxAmount - insertedAmount, transaction);
        }
        return insertedAmount;
    }

    private long insertIntoAnySlots(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        long insertedAmount = 0;
        for (TankSingleFluidStorage part : parts) {
            if (insertedAmount == maxAmount)
                break;
            insertedAmount += part.insert(insertedVariant, maxAmount - insertedAmount, transaction);
        }
        return insertedAmount;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        long extractedAmount = 0;
        for (TankSingleFluidStorage part : parts) {
            if (extractedAmount == maxAmount)
                break;
            extractedAmount += part.extract(resource, maxAmount - extractedAmount, transaction);
        }
        return extractedAmount;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return parts.stream().map(StorageView::getUnderlyingView).iterator();
    }

    public TankSingleFluidStorage getSingleFluidStorage(int index){
        return this.parts.get(index);
    }

}
