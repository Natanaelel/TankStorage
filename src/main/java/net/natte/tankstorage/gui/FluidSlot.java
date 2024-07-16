package net.natte.tankstorage.gui;

import net.minecraft.world.item.ItemStack;
import net.natte.tankstorage.storage.TankSingleFluidStorage;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidSlot extends NonBackedSlot {

    private final TankSingleFluidStorage fluidStorage;

    public FluidSlot(TankSingleFluidStorage fluidStorage, int x, int y) {
        super(x, y);
        this.fluidStorage = fluidStorage;
    }

    @Override
    public void setByPlayer(ItemStack stack) {
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    public FluidStack getFluid() {
        return fluidStorage.getFluid();
    }

    public int getAmount() {
        return fluidStorage.getAmount();
    }

    public int getCapacity() {
        return fluidStorage.getCapacity();
    }

    public boolean isLocked() {
        return fluidStorage.isLocked();
    }
}
