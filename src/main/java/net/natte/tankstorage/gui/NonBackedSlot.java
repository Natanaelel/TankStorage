package net.natte.tankstorage.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class NonBackedSlot extends Slot {

    private ItemStack stack = ItemStack.EMPTY;

    public NonBackedSlot(int x, int y) {
        super(new DummyInventory(), 0, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return false;
    }

    @Override
    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public int getMaxItemCount() {
        return 0;
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return 0;
    }
    
    @Override
    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void setStackNoCallbacks(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack takeStack(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public void markDirty() {
    }

}
