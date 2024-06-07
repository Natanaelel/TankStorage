package net.natte.tankstorage.screenhandler;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.state.TankFluidStorageState;

public class TankScreenHandlerFactory implements ExtendedScreenHandlerFactory {

    private @Nullable TankFluidStorageState tank;
    private ItemStack tankItem;
    // which inventoryslot tank is in, or -1
    private int slot;
    private ScreenHandlerContext screenHandlerContext;

    public TankScreenHandlerFactory(TankFluidStorageState tank, ItemStack tankItem, int slot,
            ScreenHandlerContext screenHandlerContext) {
        this.tank = tank;
        this.tankItem = tankItem;
        this.slot = slot;
        this.screenHandlerContext = screenHandlerContext;
    }

    @Override
    public Text getDisplayName() {
        return this.tankItem.getName();
    }

    // called server side only
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new TankScreenHandler(syncId, playerInventory,
                this.tank,
                this.tank.type,
                this.tankItem,
                this.slot,
                this.screenHandlerContext);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeString(this.tank.type.getName());
        buf.writeItemStack(this.tankItem);
        buf.writeInt(this.slot);
        buf.writeNbt(TankFluidStorageState.writeNbt(this.tank));
    }

    // called client side only
    public static TankScreenHandler createClientScreenHandler(int syncId, PlayerInventory playerInventory,
            PacketByteBuf buf) {

        TankType tankType = TankType.fromName(buf.readString());
        ItemStack tankItem = buf.readItemStack();
        int slot = buf.readInt();

        TankFluidStorageState tank = TankFluidStorageState.readNbt(buf.readNbt());

        TankScreenHandler screenHandler = new TankScreenHandler(syncId, playerInventory,
                tank,
                tankType,
                tankItem,
                slot,
                ScreenHandlerContext.EMPTY);
        return screenHandler;
    }
}
