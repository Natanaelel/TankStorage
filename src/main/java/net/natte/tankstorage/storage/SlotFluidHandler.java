package net.natte.tankstorage.storage;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class SlotFluidHandler implements IFluidHandler {

    private final TankSingleFluidStorage fluidStorage;

    public SlotFluidHandler(TankSingleFluidStorage fluidStorage) {
        this.fluidStorage = fluidStorage;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return fluidStorage.getFluid().copyWithAmount(fluidStorage.getAmount());
    }

    @Override
    public int getTankCapacity(int tank) {
        return fluidStorage.getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return fluidStorage.insert(resource, resource.getAmount(), action.simulate());
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        int extracted = fluidStorage.extract(resource, resource.getAmount(), action.simulate());
        return resource.copyWithAmount(extracted);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack resource = fluidStorage.getFluid();
        int extracted = fluidStorage.extract(resource, maxDrain, action.simulate());
        return resource.copyWithAmount(extracted);
    }
}
