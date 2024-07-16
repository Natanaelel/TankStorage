package net.natte.tankstorage.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.state.TankFluidStorageState;

public class TankMenuFactory implements MenuProvider {

    private final TankFluidStorageState tank;
    private final ItemStack tankItem;
    // which inventoryslot tank is in, or -1
    private final int slot;
    private final ContainerLevelAccess access;

    public TankMenuFactory(TankFluidStorageState tank, ItemStack tankItem, int slot,
                           ContainerLevelAccess access) {
        this.tank = tank;
        this.tankItem = tankItem;
        this.slot = slot;
        this.access = access;
    }

    public void open(Player player){
        player.openMenu(this, this::writeScreenOpeningData);
    }

    @Override
    public Component getDisplayName() {
        return this.tankItem.getHoverName();
    }

    public void writeScreenOpeningData(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(this.tank.type.getName());
        ItemStack.STREAM_CODEC.encode(buf, this.tankItem);
        buf.writeInt(this.slot);
//        buf.writeNbt(TankFluidStorageState.writeNbt(this.tank));
    }

    // called client side only
    public static TankMenu createClientScreenHandler(int syncId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {

        TankType tankType = TankType.fromName(buf.readUtf());
        ItemStack tankItem = ItemStack.STREAM_CODEC.decode(buf);
        int slot = buf.readInt();

        return new TankMenu(syncId, playerInventory,
                TankFluidStorageState.create(tankType, null), // dummy tank
                tankType,
                tankItem,
                slot,
                ContainerLevelAccess.NULL);
    }

    // called server side only
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new TankMenu(syncId, playerInventory,
                this.tank,
                this.tank.type,
                this.tankItem,
                this.slot,
                this.access);
    }
}
