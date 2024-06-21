package net.natte.tankstorage.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DummyInventory implements Container {

    @Override
    public void clearContent() {
        throw new UnsupportedOperationException("Unimplemented method 'clear'");
    }

    @Override
    public int getContainerSize() {
        throw new UnsupportedOperationException("Unimplemented method 'size'");
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Unimplemented method 'isEmpty'");
    }

    @Override
    public ItemStack getItem(int var1) {
        throw new UnsupportedOperationException("Unimplemented method 'getStack'");
    }

    @Override
    public ItemStack removeItem(int var1, int var2) {
        throw new UnsupportedOperationException("Unimplemented method 'removeStack'");
    }

    @Override
    public ItemStack removeItemNoUpdate(int var1) {
        throw new UnsupportedOperationException("Unimplemented method 'removeStack'");
    }

    @Override
    public void setItem(int var1, ItemStack var2) {
        throw new UnsupportedOperationException("Unimplemented method 'setStack'");
    }

    @Override
    public void setChanged() {
        throw new UnsupportedOperationException("Unimplemented method 'markDirty'");
    }

    @Override
    public boolean stillValid(Player var1) {
        throw new UnsupportedOperationException("Unimplemented method 'canPlayerUse'");
    }
}
