package net.natte.tankstorage.gui;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.item.ItemStack;
import net.natte.tankstorage.storage.TankSingleFluidStorage;

public class FluidSlot extends NonBackedSlot {

    private TankSingleFluidStorage fluidStorage;

    public FluidSlot(TankSingleFluidStorage fluidStorage, int x, int y) {
        super(x, y);
        this.fluidStorage = fluidStorage;
    }

    @Override
    public void setStack(ItemStack stack) {
    }

    @Override
    public ItemStack getStack() {
        return ItemStack.EMPTY;
    }

    public FluidVariant getFluidVariant() {
        return fluidStorage.getResource();
    }

    public long getAmount() {
        return fluidStorage.getAmount();
    }

    public long getCapacity() {
        return fluidStorage.getCapacity();
    }

    public boolean canInsert(FluidVariant fluidVariant) {
        return fluidStorage.canInsert(fluidVariant);
    }

    public boolean isLocked() {
        return fluidStorage.isLocked();
    }

}
