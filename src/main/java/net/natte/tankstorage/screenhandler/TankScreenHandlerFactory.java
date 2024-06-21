package net.natte.tankstorage.screenhandler;

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
import org.jetbrains.annotations.Nullable;

public class TankScreenHandlerFactory implements MenuProvider {

    private @Nullable TankFluidStorageState tank;
    private ItemStack tankItem;
    // which inventoryslot tank is in, or -1
    private int slot;
    private ContainerLevelAccess screenHandlerContext;

    public TankScreenHandlerFactory(TankFluidStorageState tank, ItemStack tankItem, int slot,
                                    ContainerLevelAccess screenHandlerContext) {
        this.tank = tank;
        this.tankItem = tankItem;
        this.slot = slot;
        this.screenHandlerContext = screenHandlerContext;
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
    public static TankScreenHandler createClientScreenHandler(int syncId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {

        TankType tankType = TankType.fromName(buf.readUtf());
        ItemStack tankItem = ItemStack.STREAM_CODEC.decode(buf);
        int slot = buf.readInt();

//        TankFluidStorageState tank = TankFluidStorageState.readNbt(buf.readNbt());

        TankScreenHandler screenHandler = new TankScreenHandler(syncId, playerInventory,
                null,
                tankType,
                tankItem,
                slot,
                ContainerLevelAccess.NULL);
        return screenHandler;
    }

    // called server side only
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new TankScreenHandler(syncId, playerInventory,
                this.tank,
                this.tank.type,
                this.tankItem,
                this.slot,
                this.screenHandlerContext);
    }

}
