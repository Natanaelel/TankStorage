package net.natte.tankstorage.screenhandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.natte.tankstorage.block.TankDockBlockEntity;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.gui.FluidSlot;
import net.natte.tankstorage.packet.screenHandler.SyncFluidPacketS2C;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.storage.TankFluidStorage;
import net.natte.tankstorage.storage.TankSingleFluidStorage;
import net.natte.tankstorage.util.FluidSlotData;
import net.natte.tankstorage.util.Util;

public class TankScreenHandler extends ScreenHandler {

    private ScreenHandlerContext context;
    private TankType tankType;
    private TankFluidStorageState tank;
    private TankFluidStorage fluidStorage;
    private Runnable onChangeListener;

    private ServerPlayerEntity player;

    private List<FluidSlotData> trackedFluids;

    public TankScreenHandler(int syncId, PlayerInventory playerInventory, TankFluidStorageState tank, TankType tankType,
            ItemStack tankItem, int slot, ScreenHandlerContext screenHandlerContext) {

        super(tankType.getScreenhandlerType(), syncId);

        this.tank = tank;
        this.tankType = tankType;
        this.context = screenHandlerContext;

        this.fluidStorage = this.tank.getFluidStorage(Util.getInsertMode(tankItem));

        if (playerInventory.player instanceof ServerPlayerEntity serverPlayerEntity) {

            this.onChangeListener = this::sendContentUpdates;

            tank.addOnChangeListener(this.onChangeListener);
            this.player = serverPlayerEntity;
            this.trackedFluids = new ArrayList<>(this.tankType.size());
            for (int i = 0; i < this.tankType.size(); ++i) {
                TankSingleFluidStorage tankSingleFluidStorage = this.fluidStorage.getSingleFluidStorage(i);
                this.trackedFluids.add(new FluidSlotData(tankSingleFluidStorage.getResource(),
                        tankSingleFluidStorage.getAmount(), tankSingleFluidStorage.isLocked()));
            }
        }

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

    public void lockSlot(int slot, FluidVariant fluidVariant, boolean shouldLock) {
        if (slot < 0 || slot >= tankType.size())
            return;
        fluidStorage.getSingleFluidStorage(slot).lock(fluidVariant, shouldLock);
    }

    private void lockSlot(int slot, FluidVariant fluidVariant) {
        lockSlot(slot, fluidVariant, true);
    }

    private void unlockSlot(int slot) {
        lockSlot(slot, null, false);
    }

    public void setLockedSlots(Map<Integer, FluidVariant> lockedSlots) {
        fluidStorage.setLockedSlots(lockedSlots);
    }

    public void lockSlotClick(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size())
            return;

        Slot slot = this.slots.get(slotIndex);
        if (!(slot instanceof FluidSlot fluidSlot))
            return;

        FluidVariant slotFluidVariant = fluidSlot.getFluidVariant();
        boolean isSlotLocked = fluidSlot.isLocked();
        boolean isSlotEmpty = fluidSlot.getAmount() == 0;
        boolean isCursorEmpty = getCursorStack().isEmpty();
        FluidVariant cursorFluidVariant = Util.getFirstFluidVariant(getCursorStack());
        boolean areFluidVariantsEqual = slotFluidVariant.equals(cursorFluidVariant);

        // slot unlocked empty 0 cursor empty -> lock empty
        // slot unlocked empty 0 cursor water -> lock water
        // slot unlocked empty 0 cursor lava -> lock lava

        // slot locked empty 0 cursor empty -> unlock
        // slot locked empty 0 cursor water -> lock water
        // slot locked empty 0 cursor lava -> lock lava

        // slot unlocked water 0 cursor empty -- invalid
        // slot unlocked water 0 cursor water -- invalid
        // slot unlocked water 0 cursor lava -- invalid

        // slot locked water 0 cursor empty -> lock empty
        // slot locked water 0 cursor water -> unlock
        // slot locked water 0 cursor lava -> lock lava

        // slot unlocked empty 1 cursor empty -- invalid
        // slot unlocked empty 1 cursor water -- invalid
        // slot unlocked empty 1 cursor lava -- invalid

        // slot locked empty 1 cursor empty -- invalid
        // slot locked empty 1 cursor water -- invalid
        // slot locked empty 1 cursor lava -- invalid

        // slot unlocked water 1 cursor empty -> lock water
        // slot unlocked water 1 cursor water -> lock water
        // slot unlocked water 1 cursor lava -> lock water

        // slot locked water 1 cursor empty -> unlock
        // slot locked water 1 cursor water -> unlock
        // slot locked water 1 cursor lava -> unlock

        if (isSlotEmpty) {
            if (isSlotLocked) {
                if (isCursorEmpty || areFluidVariantsEqual)
                    this.unlockSlot(slotIndex);
                else
                    this.lockSlot(slotIndex, cursorFluidVariant);
            } else {
                this.lockSlot(slotIndex, cursorFluidVariant);
            }
        } else {
            if (isSlotLocked) {
                this.unlockSlot(slotIndex);
            } else {
                this.lockSlot(slotIndex, slotFluidVariant);
            }
        }

    }

    // called only on server
    private void syncFluidState(int slot, ServerPlayerEntity player) {
        TankSingleFluidStorage singleFluidStorage = this.fluidStorage.getSingleFluidStorage(slot);
        this.trackedFluids.set(slot, new FluidSlotData(singleFluidStorage.getResource(), singleFluidStorage.getAmount(),
                singleFluidStorage.isLocked()));
        ServerPlayNetworking.send(player,
                new SyncFluidPacketS2C(this.syncId, slot, new FluidSlotData(singleFluidStorage.getResource(),
                        singleFluidStorage.getAmount(), singleFluidStorage.isLocked())));

    }

    @Override
    public void onClosed(PlayerEntity player) {
        if (this.onChangeListener != null)
            this.tank.removeOnChangeListener(this.onChangeListener);
        super.onClosed(player);
    }

    public void updateFluidSlot(int slot, FluidSlotData fluidSlotData) {
        if (slot < 0 || slot >= this.tankType.size())
            return;
        this.fluidStorage.getSingleFluidStorage(slot).update(fluidSlotData.amount(), fluidSlotData.fluidVariant(),
                fluidSlotData.isLocked());
    }

    @Override
    public void sendContentUpdates() {
        super.sendContentUpdates();

        for (int i = 0; i < this.trackedFluids.size(); ++i) {
            FluidSlotData trackedFluidSlot = this.trackedFluids.get(i);
            if (!trackedFluidSlot.equalsOther(this.fluidStorage.getSingleFluidStorage(i))) {
                syncFluidState(i, this.player);
            }
        }

    }

}
