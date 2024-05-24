package net.natte.tankstorage.gui;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.natte.tankstorage.storage.TankSingleFluidStorage;

public class FluidSlot extends NonBackedSlot {

    private TankSingleFluidStorage fluidStorage;

    public FluidSlot(TankSingleFluidStorage fluidStorage, int x, int y) {
        super(x, y);
        this.fluidStorage = fluidStorage;
        // this.setStack(getStack());
    }

    @Override
    public void setStack(ItemStack stack) {
        
        // System.out.println("setstack " + stack + stack.getOrCreateNbt());
        // long amount = stack.getNbt().getLong("tankstorage:amount");
        // FluidVariant fluidVariant = FluidVariant.fromNbt(stack.getNbt().getCompound("tankstorage:fluidvariant"));
        // boolean isLocked = stack.getNbt().getBoolean("tankstorage:islocked");
        // this.fluidStorage.update(amount, fluidVariant, isLocked);
    }

    @Override
    public ItemStack getStack() {
        return ItemStack.EMPTY;
        // System.out.println("getstack");
        // ItemStack stack = new ItemStack(Items.ACACIA_BOAT);
        // NbtCompound nbt = new NbtCompound();
        // nbt.putLong("tankstorage:amount", fluidStorage.getAmount());
        // nbt.put("tankstorage:fluidvariant", fluidStorage.getResource().toNbt());
        // nbt.putBoolean("tankstorage:islocked", fluidStorage.isLocked());
        // stack.setNbt(nbt);
        // // System.out.println(stack.getNbt());
        // return stack;
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
