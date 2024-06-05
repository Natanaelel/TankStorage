package net.natte.tankstorage.util;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Supplier;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.cache.CachedFluidStorageState;
import net.natte.tankstorage.cache.ClientTankCache;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.item.TankItem;
import net.natte.tankstorage.item.TankLinkItem;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.state.TankPersistentState;
import net.natte.tankstorage.state.TankStateManager;
import net.natte.tankstorage.storage.InsertMode;
import net.natte.tankstorage.storage.TankInteractionMode;
import net.natte.tankstorage.storage.TankOptions;

public class Util {

    public static Supplier<Boolean> isShiftDown = () -> false;

    private static final String UUID_KEY = "tankstorage:uuid";
    private static final String OPTIONS_KEY = "tankstorage:options";
    private static final String TYPE_KEY = "tankstorage:type";

    public static Identifier ID(String id) {
        return new Identifier(TankStorage.MOD_ID, id);
    }

    // call serverside only
    public static TankFluidStorageState getOrCreateFluidStorage(ItemStack tankItem) {
        if (!hasUUID(tankItem))
            setUUID(tankItem, UUID.randomUUID());
        UUID uuid = getUUID(tankItem);
        TankFluidStorageState tank = getFluidStorage(uuid);
        if (tank == null) {
            tank = TankFluidStorageState.create(getType(tankItem), uuid);
            TankStateManager.getState().set(uuid, tank);
        }
        return tank;
    }

    // assumes stack has uuid
    // call serverside only
    @Nullable
    public static TankFluidStorageState getFluidStorage(ItemStack tankItem) {
        return getFluidStorage(getUUID(tankItem));
    }

    // call serverside only
    @Nullable
    public static TankFluidStorageState getFluidStorage(UUID uuid) {
        TankPersistentState state = TankStateManager.getState();
        return state.get(uuid);
    }

    public static boolean hasUUID(ItemStack tankItem) {
        if (!tankItem.hasNbt())
            return false;
        if (!tankItem.getNbt().containsUuid(UUID_KEY))
            return false;
        return true;
    }

    public static UUID getUUID(ItemStack tankItem) {
        return tankItem.getNbt().getUuid(UUID_KEY);
    }

    public static void setUUID(ItemStack tankItem, UUID uuid) {
        tankItem.getOrCreateNbt().putUuid(UUID_KEY, uuid);
    }

    public static TankOptions getOrCreateOptions(ItemStack tankItem) {
        if (!tankItem.getOrCreateNbt().contains(OPTIONS_KEY))
            tankItem.getNbt().put(OPTIONS_KEY, new TankOptions().asNbt());
        return TankOptions.fromNbt(tankItem.getNbt().getCompound(OPTIONS_KEY));
    }

    public static TankOptions getOptionsOrDefault(ItemStack tankItem) {
        if (!tankItem.hasNbt())
            return new TankOptions();

        if (!tankItem.getNbt().contains(OPTIONS_KEY))
            return new TankOptions();

        return TankOptions.fromNbt(tankItem.getNbt().getCompound(OPTIONS_KEY));
    }

    public static void setOptions(ItemStack stack, TankOptions options) {
        stack.getOrCreateNbt().put(OPTIONS_KEY, options.asNbt());
    }

    public static InsertMode getInsertMode(ItemStack tankItem) {
        return getOrCreateOptions(tankItem).insertMode;
    }

    public static int getSelectedSlot(ItemStack itemStack) {
        return getOptionsOrDefault(itemStack).selectedSlot;
    }

    public static int clampSelectedSlot(ItemStack itemStack, int max) {
        TankOptions options = getOrCreateOptions(itemStack);
        options.selectedSlot = Math.min(options.selectedSlot, max);
        setOptions(itemStack, options);
        return options.selectedSlot;
    }

    private static TankType getType(ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof TankItem tankItem)
            return tankItem.type;

        if (item instanceof TankLinkItem)
            return TankType.fromName(stack.getNbt().getString(TYPE_KEY));

        return null;
    }

    public static boolean isTankLike(ItemStack stack) {
        return stack.getItem() instanceof TankItem || stack.getItem() instanceof TankLinkItem;
    }

    public static FluidVariant getFirstFluidVariant(ItemStack itemStack) {

        Storage<FluidVariant> fluidStorage = ContainerItemContext.withConstant(itemStack).find(FluidStorage.ITEM);

        if (fluidStorage == null)
            return FluidVariant.blank();

        for (StorageView<FluidVariant> fluidView : fluidStorage) {
            return fluidView.getResource();
        }

        return FluidVariant.blank();
    }

    // called only serverside
    public static void trySync(ItemStack stack, ServerPlayerEntity player) {
        if (!Util.isTankLike(stack))
            return;
        if (!Util.hasUUID(stack))
            return;
        TankFluidStorageState tank = getFluidStorage(stack);
        if (tank == null)
            return;
        tank.sync(player);
    }

    // if in bucketmode: only allow extraction of selected fluid
    @Nullable
    public static Storage<FluidVariant> getFluidStorageFromItemContext(ItemStack itemStack,
            ContainerItemContext containerItemContext) {

        if (!Util.hasUUID(itemStack))
            return null;

        TankOptions options = Util.getOrCreateOptions(itemStack);
        InsertMode insertMode = options.insertMode;
        int selectedslot = options.selectedSlot;
        TankInteractionMode interactionMode = options.interactionMode;

        boolean isClient = Thread.currentThread().getName().equals("Render thread");

        if (isClient) {
            CachedFluidStorageState cached = ClientTankCache.getOrQueueUpdate(Util.getUUID(itemStack));
            if (cached == null)
                return null;

            if (interactionMode == TankInteractionMode.BUCKET) {
                if (selectedslot == -1) {
                    return FilteringStorage.insertOnlyOf(cached.getFluidStorage(insertMode));
                } else {
                    selectedslot = Math.min(selectedslot, cached.getNonEmptyFluids().size() - 1);
                    FluidVariant selectedFluid = selectedslot == -1 ? FluidVariant.blank()
                            : cached.getNonEmptyFluids().get(selectedslot).fluidVariant();
                    return FluidStorageUtil.filteredExtraction(cached.getFluidStorage(insertMode), selectedFluid);
                }
            }

            return cached.getFluidStorage(insertMode);
        } else {
            TankFluidStorageState tank = getFluidStorage(itemStack);

            if (interactionMode == TankInteractionMode.BUCKET) {
                if (selectedslot == -1) {
                    return FilteringStorage.insertOnlyOf(tank.getFluidStorage(insertMode));
                } else {
                    List<FluidSlotData> fluids = tank.getNonEmptyFluids();
                    selectedslot = clampSelectedSlot(itemStack, fluids.size() - 1);
                    FluidVariant selectedFluid = selectedslot == -1 ? FluidVariant.blank()
                            : fluids.get(selectedslot).fluidVariant();
                    return FluidStorageUtil.filteredExtraction(tank.getFluidStorage(insertMode), selectedFluid);
                }
            }
            return tank.getFluidStorage(insertMode);
        }
    }

    @Nullable
    public static Storage<FluidVariant> getFluidStorageFromItem(ItemStack itemStack) {
        return getFluidStorageFromItemContext(itemStack, ContainerItemContext.withConstant(itemStack));

    }

    @Nullable
    public static FluidVariant getSelectedFluid(ItemStack itemStack) {

        if (!Util.hasUUID(itemStack))
            return null;

        TankOptions options = Util.getOrCreateOptions(itemStack);
        int selectedslot = options.selectedSlot;
        boolean isClient = Thread.currentThread().getName().equals("Render thread");

        if (isClient) {
            CachedFluidStorageState cached = ClientTankCache.getOrQueueUpdate(Util.getUUID(itemStack));
            if (cached == null)
                return null;

            selectedslot = Math.min(selectedslot, cached.getNonEmptyFluids().size() - 1);
            return selectedslot == -1 ? null
                    : cached.getNonEmptyFluids().get(selectedslot).fluidVariant();

        } else {
            TankFluidStorageState tank = getFluidStorage(itemStack);

            List<FluidSlotData> fluids = tank.getNonEmptyFluids();
            selectedslot = clampSelectedSlot(itemStack, fluids.size() - 1);
            return selectedslot == -1 ? null
                    : fluids.get(selectedslot).fluidVariant();
        }
    }

    // assumes storage exists
    public static Storage<FluidVariant> getFluidStorageServer(ItemStack itemStack) {
        return getFluidStorage(itemStack).getFluidStorage(getInsertMode(itemStack));
    }

    public static Storage<FluidVariant> getFluidStorageClient(ItemStack itemStack) {
        CachedFluidStorageState cached = ClientTankCache.getOrQueueUpdate(Util.getUUID(itemStack));
        if (cached == null)
            return Storage.empty();
        return cached.getFluidStorage(getInsertMode(itemStack));
    }

    public static Storage<FluidVariant> getFluidStorage(ItemStack itemStack, boolean isClient) {
        return isClient ? getFluidStorageClient(itemStack) : getFluidStorageServer(itemStack);
    }

    public static void clampSelectedSlotServer(ItemStack stack) {
        int max = getFluidStorage(stack).getNonEmptyFluidsSize() - 1;
        clampSelectedSlot(stack, max);
    }
}
