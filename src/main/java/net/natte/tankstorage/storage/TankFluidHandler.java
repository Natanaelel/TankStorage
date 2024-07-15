package net.natte.tankstorage.storage;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.List;

public class TankFluidHandler implements IFluidHandlerItem {

    private boolean canExtract = true;
    private FluidStack extractFilter = null;

    private final List<TankSingleFluidStorage> parts;
    private final InsertMode insertMode;
    private ItemStack item = ItemStack.EMPTY;

    public TankFluidHandler(List<TankSingleFluidStorage> parts, InsertMode insertMode) {
        this.parts = parts;
        this.insertMode = insertMode;
    }

    public TankFluidHandler withItem(ItemStack item) {
        this.item = item;
        return this;
    }

    public TankFluidHandler insertOnly() {
        this.canExtract = false;
        return this;
    }

    public TankFluidHandler extractOnly(FluidStack extractFilter) {
        this.extractFilter = extractFilter;
        return this;
    }

    @Override
    public int getTanks() {
        return parts.size();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        TankSingleFluidStorage singleTank = parts.get(tank);
        if (singleTank.getAmount() == 0)
            return FluidStack.EMPTY;
        return singleTank.getFluid().copyWithAmount(singleTank.getAmount());
    }

    @Override
    public int getTankCapacity(int tank) {
        return parts.get(tank).getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty())
            return 0;
        if (extractFilter != null && !FluidStack.isSameFluidSameComponents(extractFilter, resource))
            return 0;
        int maxAmount = resource.getAmount();
        int inserted = 0;
        switch (insertMode) {
            case ALL -> {
                inserted += fillAnySlots(resource, maxAmount - inserted, action.simulate());
            }
            case FILTERED -> {
                inserted += fillLockedSlots(resource, maxAmount - inserted, action.simulate());
                inserted += fillNonEmptySlots(resource, maxAmount - inserted, action.simulate());
                if (hasSlotWithFluid(resource))
                    inserted += fillAnySlots(resource, maxAmount - inserted, action.simulate());
            }
            case VOID_OVERFLOW -> {
                inserted += fillLockedSlots(resource, maxAmount - inserted, action.simulate());
                inserted += fillNonEmptySlots(resource, maxAmount - inserted, action.simulate());
                if (hasSlotWithFluid(resource))
                    inserted = maxAmount;
            }
        }
        return inserted;
    }

    private int fillLockedSlots(FluidStack resource, int maxAmount, boolean simulate) {
        if (maxAmount == 0)
            return 0;
        int inserted = 0;
        for (TankSingleFluidStorage tank : parts) {
            if (tank.isLocked())
                inserted += tank.insert(resource, maxAmount - inserted, simulate);
        }
        return inserted;
    }

    private int fillNonEmptySlots(FluidStack resource, int maxAmount, boolean simulate) {
        if (maxAmount == 0)
            return 0;
        int inserted = 0;
        for (TankSingleFluidStorage tank : parts) {
            if (tank.getAmount() > 0)
                inserted += tank.insert(resource, maxAmount - inserted, simulate);
        }
        return inserted;
    }


    private int fillAnySlots(FluidStack resource, int maxAmount, boolean simulate) {
        if (maxAmount == 0)
            return 0;
        int inserted = 0;
        for (TankSingleFluidStorage tank : parts) {
            inserted += tank.insert(resource, maxAmount - inserted, simulate);
        }
        return inserted;
    }

    private boolean hasSlotWithFluid(FluidStack resource) {
        for (TankSingleFluidStorage tank : parts) {
            if (FluidStack.isSameFluidSameComponents(tank.getFluid(), resource))
                return true;
        }
        return false;
    }


    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (!canExtract(resource))
            return FluidStack.EMPTY;
        int maxAmount = resource.getAmount();
        int extracted = 0;
        for (TankSingleFluidStorage tank : parts) {
            extracted += tank.extract(resource, maxAmount - extracted, action.simulate());
        }
        return resource.copyWithAmount(extracted);
    }


    @Override
    public FluidStack drain(int maxAmount, FluidAction action) {
        if (!canExtract)
            return FluidStack.EMPTY;
        FluidStack resource = extractFilter;
        int extracted = 0;
        for (TankSingleFluidStorage tank : parts) {
            if (resource == null && !tank.getFluid().isEmpty() && tank.getAmount() > 0)
                resource = tank.getFluid();
            if (resource != null)
                extracted += tank.extract(resource, maxAmount - extracted, action.simulate());
        }
        return extracted == 0 ? FluidStack.EMPTY : resource.copyWithAmount(extracted);
    }

    private boolean canExtract(FluidStack resource) {
        if (!canExtract)
            return false;
        if (extractFilter == null)
            return true;
        return FluidStack.isSameFluidSameComponents(extractFilter, resource);
    }

    @Override
    public ItemStack getContainer() {
        return item;
    }
}
