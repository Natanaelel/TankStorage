package net.natte.tankstorage.screenhandler;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.natte.tankstorage.block.TankDockBlockEntity;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.gui.FluidSlot;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.storage.TankFluidStorage;
import net.natte.tankstorage.storage.TankSingleFluidStorage;
import net.natte.tankstorage.util.Util;

public class TankScreenHandler extends ScreenHandler {

    private ScreenHandlerContext context;
    private TankType tankType;
    private TankFluidStorageState tank;
    private TankFluidStorage fluidStorage;

    public TankScreenHandler(int syncId, PlayerInventory playerInventory, TankFluidStorageState tank, TankType tankType,
            ItemStack tankItem, int slot, ScreenHandlerContext screenHandlerContext) {

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
            // cannot move opened tank
            if (slot != -1 && slot == x)
                this.addSlot(new Slot(playerInventory, x, 8 + x * 18, inventoryY + 58) {
                    @Override
                    public boolean canTakeItems(PlayerEntity playerEntity) {
                        return false;
                    }
                });
            else
                this.addSlot(new Slot(playerInventory, x, 8 + x * 18, inventoryY + 58));
        }
    }

    // when shift clicking on an item containing fluids, try to insert that fluid
    // into max 1 fluid slot
    @Override
    public ItemStack quickMove(PlayerEntity playerEntity, int slotIndex) {

        Slot slot = this.slots.get(slotIndex);
        if (slot instanceof FluidSlot)
            return ItemStack.EMPTY;

        ContainerItemContext containerItemContext = ContainerItemContext.ofPlayerSlot(playerEntity,
                PlayerInventoryStorage.of(playerEntity).getSlot(slot.getIndex()));

        Storage<FluidVariant> stackFluidStorage = containerItemContext.find(FluidStorage.ITEM);

        FluidVariant insertedFluidVariant = this.fluidStorage.quickInsert(stackFluidStorage);

        if (insertedFluidVariant != null)
            playerEntity.playSound(FluidVariantAttributes.getEmptySound(insertedFluidVariant), SoundCategory.BLOCKS, 1,
                    1);

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

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {

        // if (slotIndex < 0)
        // return;

        Slot slot = slotIndex >= 0 ? this.slots.get(slotIndex) : null;

        if (!(slot instanceof FluidSlot)) {
            super.onSlotClick(slotIndex, button, actionType, player);
            return;
        }
        if (slotIndex < 0)
            return;

        if (actionType != SlotActionType.PICKUP)
            return;

        // this assumes fluidslots come first
        TankSingleFluidStorage slotFluidStorage = this.fluidStorage.getSingleFluidStorage(slotIndex);
        ContainerItemContext containerItemContext = ContainerItemContext.ofPlayerCursor(player, this);
        Storage<FluidVariant> cursorFluidStorage = containerItemContext.find(FluidStorage.ITEM);

        if (cursorFluidStorage == null)
            return;

        if (button == 1) {
            // insert into tank from cursor
            if (!cursorFluidStorage.supportsExtraction())
                return;

            try (Transaction transaction = Transaction.openOuter()) {
                for (StorageView<FluidVariant> cursorFluidView : cursorFluidStorage.nonEmptyViews()) {

                    FluidVariant fluidVariant = cursorFluidView.getResource();
                    long maxAmount = cursorFluidView.getAmount();

                    long inserted = slotFluidStorage.insert(fluidVariant, maxAmount, transaction);
                    long extracted = cursorFluidView.extract(fluidVariant, inserted, transaction);

                    if (inserted > 0) {
                        // *should* always be true
                        if (extracted == inserted) {
                            transaction.commit();
                            player.playSound(
                                    FluidVariantAttributes.getEmptySound(fluidVariant), SoundCategory.BLOCKS, 1, 1);
                        } else {
                            transaction.abort();
                        }
                        break;

                    }
                }
            }
        } else {
            // extract from tank into cursor
            if (!cursorFluidStorage.supportsInsertion())
                return;

            try (Transaction transaction = Transaction.openOuter()) {
                // for (StorageView<FluidVariant> cursorFluidView : cursorFluidStorage) {
                FluidVariant fluidVariant = slotFluidStorage.getResource();
                if (fluidVariant.isBlank())
                    return;

                long maxAmount = slotFluidStorage.getAmount();

                long inserted = cursorFluidStorage.insert(fluidVariant, maxAmount, transaction);
                long extracted = slotFluidStorage.extract(fluidVariant, inserted, transaction);

                if (inserted > 0) {
                    // *should* always be true
                    if (extracted == inserted) {
                        transaction.commit();
                        player.playSound(
                                FluidVariantAttributes.getFillSound(fluidVariant), SoundCategory.BLOCKS, 1, 1);
                    } else {
                        transaction.abort();
                    }

                }
            }
        }
    }

}
