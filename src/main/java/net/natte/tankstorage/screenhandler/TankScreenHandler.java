package net.natte.tankstorage.screenhandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.block.TankDockBlockEntity;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.gui.FluidSlot;
import net.natte.tankstorage.gui.LockedSlot;
import net.natte.tankstorage.packet.screenHandler.SyncFluidPacketS2C;
import net.natte.tankstorage.packet.server.LockSlotPacketC2S;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.storage.TankFluidHandler;
import net.natte.tankstorage.storage.TankSingleFluidStorage;
import net.natte.tankstorage.util.FluidSlotData;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TankScreenHandler extends AbstractContainerMenu {


    private ContainerLevelAccess context;

    private ItemStack tankItem;
    private TankType tankType;
    private TankFluidStorageState tank;

    @Nullable
    private TankFluidHandler fluidStorage;
    private int slotWithOpenedTank;

    private Runnable onChangeListener;

    @Nullable
    private ServerPlayer player;

    private List<FluidSlotData> trackedFluids;

    public TankScreenHandler(int syncId, Inventory playerInventory, TankFluidStorageState tank, TankType tankType,
                             ItemStack tankItem, int slot, ContainerLevelAccess screenHandlerContext) {
        super(TankStorage.TANK_MENU.get(), syncId);

        this.tankItem = tankItem;
        this.tank = tank;
        this.tankType = tankType;
        this.slotWithOpenedTank = slot;
        this.context = screenHandlerContext;

        this.fluidStorage = this.tank.getFluidHandler(Util.getInsertMode(tankItem));

        if (playerInventory.player instanceof ServerPlayer serverPlayerEntity) {

            this.onChangeListener = this::broadcastChanges;

            tank.addOnChangeListener(this.onChangeListener);
            this.player = serverPlayerEntity;
            this.trackedFluids = new ArrayList<>(this.tankType.size());
            for (int i = 0; i < this.tankType.size(); ++i) {
                TankSingleFluidStorage tankSingleFluidStorage = this.tank.getPart(i);
                this.trackedFluids.add(FluidSlotData.from(tankSingleFluidStorage));
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
                this.addSlot(new FluidSlot(this.tank.getPart(slotIndex), slotX, slotY));
            }
        }

        // player inventory
        int inventoryY = 32 + rows * 18;
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                // cannot move opened tank
                if (slot == x + y * 9 + 9)
                    this.addSlot(new LockedSlot(playerInventory, x + y * 9 + 9, 8 + x * 18, inventoryY + y * 18));
                else
                    this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, inventoryY + y * 18));
            }
        }

        // hotbar
        for (int x = 0; x < 9; ++x) {
            // cannot move opened tank
            if (slot == x)
                this.addSlot(new LockedSlot(playerInventory, x, 8 + x * 18, inventoryY + 58));
            else
                this.addSlot(new Slot(playerInventory, x, 8 + x * 18, inventoryY + 58));
        }
    }

    public ContainerLevelAccess getContext() {
        return context;
    }

    public ItemStack getTankItem() {
        return tankItem;
    }

    // when shift clicking on an item containing fluids, try to insert that fluid
    // into max 1 fluid slot
    // TODO: restore
//    @Override
    public ItemStack quickMoveStack(Player playerEntity, int slotIndex) {
        return ItemStack.EMPTY;
    }
//
//        Slot slot = this.slots.get(slotIndex);
//        if (slot instanceof FluidSlot)
//            return ItemStack.EMPTY;
//
//        ContainerLevelAccess containerItemContext = ContainerLevelAccess.ofPlayerSlot(playerEntity,
//                PlayerInventoryStorage.of(playerEntity).getSlot(slot.getIndex()));
//
//        Storage<FluidVariant> stackFluidStorage = containerItemContext.find(FluidStorage.ITEM);
//
//        FluidVariant insertedFluidVariant = this.fluidStorage.quickInsert(stackFluidStorage);
//
//        if (insertedFluidVariant != null) {
//            playerEntity.playSound(FluidVariantAttributes.getEmptySound(insertedFluidVariant), SoundCategory.BLOCKS, 1,
//                    1);
//            // update client cache for tooltip contents if other is another tank
//            if (!playerEntity.getWorld().isClient()) {
//                this.tank.sync(player);
//                Util.trySync(slot.getStack(), player);
//                // force sync all fluid slots
//                for (int i = 0; i < this.trackedFluids.size(); ++i)
//                    syncFluidState(i, this.player);
//
//            }
//            return ItemStack.EMPTY;
//        } else {
//            // if no fluids moved, normal quickMove
//            ItemStack newStack = ItemStack.EMPTY;
//            if (slot != null && slot.hasStack()) {
//                ItemStack originalStack = slot.getStack();
//                newStack = originalStack.copy();
//                if (slotIndex < this.tankType.size())
//                    return ItemStack.EMPTY;
//                if (slotIndex < this.slots.size() - 9) {
//                    if (!this.insertItem(originalStack, this.slots.size() - 9, this.slots.size(), false)) {
//                        return ItemStack.EMPTY;
//                    }
//                } else if (!this.insertItem(originalStack, this.tankType.size(), this.slots.size() - 9, false)) {
//                    return ItemStack.EMPTY;
//                }
//
//                if (originalStack.isEmpty()) {
//                    slot.setStack(ItemStack.EMPTY);
//                } else {
//                    slot.markDirty();
//                }
//            }
//
//            return newStack;
//            // return super.quickMove(playerEntity, slotIndex);
//        }
//
//    }


    @Override
    public boolean stillValid(Player player) {
        if (!AbstractContainerMenu.stillValid(this.context, player, TankStorage.TANK_DOCK_BLOCK.get()))
            return false;

        return this.context.evaluate((world, pos) -> {
            if (!(world.getBlockEntity(pos) instanceof TankDockBlockEntity blockEntity))
                return false;
            if (!blockEntity.hasTank())
                return false;
            if (!blockEntity.getTank().has(TankStorage.UUIDComponentType))
                return false;
            if (!blockEntity.getTank().get(TankStorage.UUIDComponentType).equals(this.tank.uuid))
                return false;
            return true;
        }, true);
    }


    @Override
    public void clicked(int slotIndex, int button, ClickType actionType, Player player) {

        if (actionType == ClickType.SWAP) {
            // cannot move opened TankItem with numbers
            if (!this.slots.get(slotIndex).mayPickup(player) || button == slotWithOpenedTank)
                return;
        }


        Slot slot = slotIndex >= 0 ? this.slots.get(slotIndex) : null;


        if (!(slot instanceof FluidSlot)) {
            super.clicked(slotIndex, button, actionType, player);
            return;
        }

        if (actionType != ClickType.PICKUP && actionType != ClickType.PICKUP_ALL)
            return;

        Level world = player.level();
        if (world.isClientSide)
            return;

        // TODO: fluid interaction
//
//        // this assumes fluidslots come first
//        TankSingleFluidStorage slotFluidStorage = this.fluidStorage.getSingleFluidStorage(slotIndex);
//        ContainerItemContext containerItemContext = ContainerItemContext.ofPlayerCursor(playerEntity, this);
//
//        Storage<FluidVariant> cursorFluidStorage = containerItemContext.find(FluidStorage.ITEM);
//
//        if (cursorFluidStorage == null)
//            return;
//
//        if (button == 1) {
//            // insert into tank from cursor
//            if (!cursorFluidStorage.supportsExtraction())
//                return;
//
//            try (Transaction transaction = Transaction.openOuter()) {
//                for (StorageView<FluidVariant> cursorFluidView : cursorFluidStorage.nonEmptyViews()) {
//
//                    FluidVariant fluidVariant = cursorFluidView.getResource();
//                    long maxAmount = cursorFluidView.getAmount();
//
//                    long inserted = slotFluidStorage.insert(fluidVariant, maxAmount, transaction);
//                    long extracted = cursorFluidView.extract(fluidVariant, inserted, transaction);
//
//                    if (inserted > 0) {
//                        // *should* always be true
//                        if (extracted == inserted) {
//                            transaction.commit();
//                            playerEntity.playSound(
//                                    FluidVariantAttributes.getEmptySound(fluidVariant), SoundCategory.BLOCKS, 1, 1);
//                            this.tank.sync(player);
//                            Util.trySync(getCursorStack(), player);
//                        } else {
//                            transaction.abort();
//                        }
//                        break;
//                    }
//                }
//            }
//        } else {
//            // extract from tank into cursor
//            if (!cursorFluidStorage.supportsInsertion())
//                return;
//
//            try (Transaction transaction = Transaction.openOuter()) {
//                FluidVariant fluidVariant = slotFluidStorage.getResource();
//                if (fluidVariant.isBlank())
//                    return;
//
//                long maxAmount = slotFluidStorage.getAmount();
//                if (Util.isTankLike(this.getCursorStack()))
//                    maxAmount = Math.min(maxAmount, Util.getType(this.getCursorStack()).getCapacity());
//
//                long inserted = cursorFluidStorage.insert(fluidVariant, maxAmount, transaction);
//                long extracted = slotFluidStorage.extract(fluidVariant, inserted, transaction);
//
//                if (inserted > 0) {
//                    // *should* always be true
//                    if (extracted == inserted) {
//                        transaction.commit();
//                        player.playSound(
//                                FluidVariantAttributes.getFillSound(fluidVariant), SoundCategory.BLOCKS, 1, 1);
//                        this.tank.sync((ServerPlayerEntity) player);
//                        Util.trySync(getCursorStack(), (ServerPlayerEntity) player);
//                    } else {
//                        transaction.abort();
//                    }
//                }
//            }
//        }
    }

    // returns whether lock was valid
    public boolean lockSlot(int slot, FluidStack fluidVariant, boolean shouldLock) {
        if (slot < 0 || slot >= this.slots.size())
            return false;
        if (!(this.slots.get(slot) instanceof FluidSlot fluidSlot))
            return false;
        if (shouldLock && fluidSlot.getAmount() > 0 && !FluidStack.isSameFluidSameComponents(fluidSlot.getFluid(), fluidVariant))
            return false;
        tank.getPart(slot).lock(fluidVariant, shouldLock);
        return true;
    }

    public boolean lockSlot(int slot, FluidStack fluidVariant) {
        return lockSlot(slot, fluidVariant, true);
    }

    public boolean unlockSlot(int slot) {
        return lockSlot(slot, null, false);
    }

    public void handleSlotLock(FluidSlot slot, ItemStack carried) {

        FluidStack cursorFluid = FluidUtil.getFluidContained(carried).orElse(FluidStack.EMPTY);
        int hoveredSlotIndex = slot.index;

        FluidStack slotFluid = slot.getFluid();
        boolean isSlotEmpty = slot.getAmount() == 0;

        boolean shouldUnLock = slot.isLocked() && (cursorFluid.isEmpty() || slot.getAmount() > 0 || FluidStack.isSameFluidSameComponents(cursorFluid, slotFluid));

        // optimistically lock slot on client, will be synced later
        if (shouldUnLock)
            unlockSlot(slot.index);
        else
            lockSlot(slot.index, isSlotEmpty ? cursorFluid : slotFluid);

        PacketDistributor.sendToServer(new LockSlotPacketC2S(
                containerId,
                hoveredSlotIndex,
                isSlotEmpty ? cursorFluid : slotFluid,
                !shouldUnLock));
    }

    // called only on server
    private void syncFluidSlot(int slot, ServerPlayer player) {
        TankSingleFluidStorage singleFluidStorage = this.tank.getPart(slot);
        this.trackedFluids.set(slot, FluidSlotData.from(singleFluidStorage));
        PacketDistributor.sendToPlayer(player, new SyncFluidPacketS2C(this.containerId, slot, FluidSlotData.from(singleFluidStorage)));
    }

    @Override
    public void removed(Player player) {
        if (this.onChangeListener != null)
            this.tank.removeOnChangeListener(this.onChangeListener);
        super.removed(player);
    }

    public void updateFluidSlot(int slot, FluidSlotData fluidSlotData) {
        if (slot < 0 || slot >= this.tankType.size())
            return;
        this.tank.getPart(slot).update(fluidSlotData.amount(), fluidSlotData.fluidVariant(),
                fluidSlotData.isLocked());
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        for (int i = 0; i < this.trackedFluids.size(); ++i) {
            FluidSlotData trackedFluidSlot = this.trackedFluids.get(i);
            if (!trackedFluidSlot.equalsOther(this.tank.getPart(i)))
                syncFluidSlot(i, this.player);
        }
    }

    public TankType getTankType() {
        return this.tankType;
    }
}
