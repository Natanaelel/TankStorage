package net.natte.tankstorage.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DummyInventory implements Container {

    @Override
    public void clearContent() {
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ItemStack getItem(int var1) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int var1, int var2) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int var1) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int var1, ItemStack var2) {
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player var1) {
        return true;
    }
}
