package net.natte.tankstorage.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class DummyInventory implements Inventory {

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unimplemented method 'clear'");
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Unimplemented method 'size'");
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Unimplemented method 'isEmpty'");
    }

    @Override
    public ItemStack getStack(int var1) {
        throw new UnsupportedOperationException("Unimplemented method 'getStack'");
    }

    @Override
    public ItemStack removeStack(int var1, int var2) {
        throw new UnsupportedOperationException("Unimplemented method 'removeStack'");
    }

    @Override
    public ItemStack removeStack(int var1) {
        throw new UnsupportedOperationException("Unimplemented method 'removeStack'");
    }

    @Override
    public void setStack(int var1, ItemStack var2) {
        throw new UnsupportedOperationException("Unimplemented method 'setStack'");
    }

    @Override
    public void markDirty() {
        throw new UnsupportedOperationException("Unimplemented method 'markDirty'");
    }

    @Override
    public boolean canPlayerUse(PlayerEntity var1) {
        throw new UnsupportedOperationException("Unimplemented method 'canPlayerUse'");
    }
}
