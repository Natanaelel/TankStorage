package net.natte.tankstorage.storage;

import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class TankFluidStorage implements Storage<FluidVariant> {

    private List<TankSingleFluidStorage> parts;
    private InsertMode insertMode;

    public boolean isDirty = false;
    private Runnable onMarkDirty;

    public TankFluidStorage(List<TankSingleFluidStorage> parts, InsertMode insertMode) {
        this.parts = parts;
        this.insertMode = insertMode;
    }

    public void setMarkDirtyListener(Runnable listener) {
        this.onMarkDirty = listener;
        this.parts.forEach(part -> part.setMarkDirtyListener(this::markDirty));
    }

    @Override
    public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        if (maxAmount == 0)
            return 0;

        long insertedAmount = 0;

        switch (insertMode) {
            case ALL:
                insertedAmount += insertIntoLockedSlots(insertedVariant, maxAmount - insertedAmount, transaction);
                insertedAmount += insertIntoNonEmptySlots(insertedVariant, maxAmount - insertedAmount, transaction);
                insertedAmount += insertIntoAnySlots(insertedVariant, maxAmount - insertedAmount, transaction);
                break;
            case FILTERED:
                insertedAmount += insertIntoLockedSlots(insertedVariant, maxAmount - insertedAmount, transaction);
                insertedAmount += insertIntoNonEmptySlots(insertedVariant, maxAmount - insertedAmount, transaction);
                if (hasSlotWithVariant(insertedVariant))
                    insertedAmount += insertIntoAnySlots(insertedVariant, maxAmount - insertedAmount, transaction);
                break;

            case VOID_OVERFLOW:
                insertedAmount += insertIntoLockedSlots(insertedVariant, maxAmount - insertedAmount, transaction);
                insertedAmount += insertIntoNonEmptySlots(insertedVariant, maxAmount - insertedAmount, transaction);
                insertedAmount = maxAmount;
                break;
        }

        return insertedAmount;
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
        if (maxAmount == 0)
            return 0;

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
        return parts.stream().map(x -> (StorageView<FluidVariant>) x).iterator();
    }

    public TankSingleFluidStorage getSingleFluidStorage(int index) {
        return this.parts.get(index);
    }

    private void markDirty() {
        this.isDirty = true;
        if (this.onMarkDirty != null)
            this.onMarkDirty.run();
    }

    // returns inserted fluidvariant, or null if none inserted
    @Nullable
    public FluidVariant quickInsert(@Nullable Storage<FluidVariant> itemFluidStorage) {

        if (itemFluidStorage == null)
            return null;

        if (!itemFluidStorage.supportsExtraction())
            return null;

        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<FluidVariant> fluidView : itemFluidStorage.nonEmptyViews()) {
                FluidVariant fluidVariant = fluidView.getResource();
                long maxAmount = fluidView.getAmount();

                long inserted = this.quickInsert(fluidVariant, maxAmount, transaction);
                long extracted = fluidView.extract(fluidVariant, inserted, transaction);

                if (inserted > 0) {
                    // *should* always be true
                    if (extracted == inserted) {
                        transaction.commit();
                        return fluidVariant;
                    } else {
                        transaction.abort();
                    }
                    break;
                }
            }
        }
        return null;
    }

    // insert into max 1 slot, first try locked, then nonempty, then any.
    private long quickInsert(FluidVariant fluidVariant, long maxAmount, TransactionContext transaction) {

        // first try to insert insert into locked slot
        for (TankSingleFluidStorage part : parts) {
            if (!part.isLocked())
                continue;
            long inserted = part.insert(fluidVariant, maxAmount, transaction);
            if (inserted > 0)
                return inserted;
        }

        // then into slots already containing fluids
        for (TankSingleFluidStorage part : parts) {
            if (part.getAmount() == 0)
                continue;
            long inserted = part.insert(fluidVariant, maxAmount, transaction);
            if (inserted > 0)
                return inserted;
        }

        // then any slot
        for (TankSingleFluidStorage part : parts) {
            long inserted = part.insert(fluidVariant, maxAmount, transaction);
            if (inserted > 0)
                return inserted;
        }

        return 0;
    }
}
