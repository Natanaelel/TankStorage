package net.natte.tankstorage.screenhandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.natte.tankstorage.block.TankDockBlockEntity;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.gui.FluidSlot;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.storage.TankFluidStorage;
import net.natte.tankstorage.util.Util;

public class TankScreenHandler extends ScreenHandler {

    private ScreenHandlerContext context;
    private TankType tankType;
    private TankFluidStorageState tank;
    private TankFluidStorage fluidStorage;

    public TankScreenHandler(int syncId, PlayerInventory playerInventory, TankFluidStorageState tank, TankType tankType,
            ItemStack tankItem, ScreenHandlerContext screenHandlerContext) {

        super(tankType.getScreenhandlerType(), syncId);

        this.tank = tank;
        this.tankType = tankType;
        this.context = screenHandlerContext;
        
        this.fluidStorage = this.tank.getFluidStorage(Util.getInsertMode(tankItem));

        int rows = this.tankType.height();
        int cols = this.tankType.width();

        // tank
        for (int y = 0; y < rows; ++y) {
            for (int x = 0; x < cols; ++x) {
                int slotIndex = x + y * cols;
                int slotX = 8 + x * 18 + (9 - cols) * 9;
                int slotY = 18 + y * 18;
                this.addSlot(new FluidSlot(this.fluidStorage.getSingleFluidStorage(slotIndex), slotX, slotY));
            }
        }

        // player inventory
        int inventoryY = 32 + rows * 18;
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, inventoryY + y * 18));
            }
        }

        // hotbar
        for (int x = 0; x < 9; ++x) {
            // TODO: v
            // cannot move opened bank
            // if (playerInventory.selectedSlot == x
            // && Util.isTankLike(playerInventory.getStack(playerInventory.selectedSlot))
            // && this.context == ScreenHandlerContext.EMPTY) {
            // this.addSlot(new LockedSlot(playerInventory, x, 8 + x * 18, inventoryY +
            // 58));
            // } else {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, inventoryY + 58));
            // }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity playerEntity, int slot) {
        // TODO quickmove fluids
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity var1) {
        return this.context.get((world, pos) -> {
            if (!(world.getBlockEntity(pos) instanceof TankDockBlockEntity blockEntity))
                return false;
            if (!blockEntity.hasTank())
                return false;
            return true;
        }, true);
    }

}
