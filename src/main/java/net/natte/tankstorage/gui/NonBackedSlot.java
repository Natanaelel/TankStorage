package net.natte.tankstorage.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;


public class NonBackedSlot extends Slot {
    private ItemStack stack = ItemStack.EMPTY;

    public NonBackedSlot(int x, int y) {
        super(new DummyInventory(), 0, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return this.stack;
    }

    @Override
    public int getMaxStackSize() {
        return 0;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 0;
    }

    @Override
    public void setByPlayer(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void set(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setChanged() {
    }
}
