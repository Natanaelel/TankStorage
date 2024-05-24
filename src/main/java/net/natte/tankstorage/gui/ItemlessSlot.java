package net.natte.tankstorage.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

public class ItemlessSlot extends Slot {

    
    public ItemlessSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
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
        return Items.ACACIA_BOAT.getDefaultStack();
        // return ItemStack.EMPTY;
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
    }

    @Override
    public void setStackNoCallbacks(ItemStack stack) {
    }

    @Override
    public ItemStack takeStack(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public void markDirty() {
    }

}
