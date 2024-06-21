package net.natte.tankstorage.util;

import java.util.List;
import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Supplier;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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
    public static final String TYPE_KEY = "tankstorage:type";

    public static ResourceLocation ID(String id) {
        return ResourceLocation.fromNamespaceAndPath(TankStorage.MOD_ID, id);
    }

    // call serverside only. returns null if unlinked linkitem
    public static TankFluidStorageState getOrCreateFluidStorage(ItemStack tankItem) {
        if (isLink(tankItem) && !hasUUID(tankItem))
            return null;
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
        stack.set(TankStorage.OptionsComponentType, options);
    }

    public static InsertMode getInsertMode(ItemStack tankItem) {
        return tankItem.getOrDefault(TankStorage.OptionsComponentType, TankOptions.DEFAULT).insertMode();
    }

    public static int getSelectedSlot(ItemStack itemStack) {
        return itemStack.getOrDefault(TankStorage.SelectedSlotComponentType, -1);
    }

    public static int clampSelectedSlot(ItemStack itemStack, int max) {
        int selectedSlot = getSelectedSlot(itemStack);
        int clampedSelectedSlot = Math.min(selectedSlot, max);
        if (clampedSelectedSlot != selectedSlot)
            itemStack.set(TankStorage.SelectedSlotComponentType, clampedSelectedSlot);
        return clampedSelectedSlot;
    }

    public static TankType getType(ItemStack stack) {
        if (stack.getItem() instanceof TankItem tankItem)
            return tankItem.type;
        return stack.getOrDefault(TankStorage.TankTypeComponentType, TankStorage.TANK_TYPES[0]);
    }

    public static void setType(ItemStack stack, TankType type) {
        stack.set(TankStorage.TankTypeComponentType, type);
    }

    public static boolean isTank(ItemStack stack) {
        return stack.getItem() instanceof TankItem;
    }

    public static boolean isLink(ItemStack stack) {
        return stack.getItem() instanceof TankLinkItem;
    }

    public static boolean isTankLike(ItemStack stack) {
        return isTank(stack) || isLink(stack);
    }


    public static FluidStack getFirstFluid(ItemStack itemStack) {

        IFluidHandlerItem fluidHandler = itemStack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler == null)
            return FluidStack.EMPTY;

        if (fluidHandler.getTanks() == 0)
            return FluidStack.EMPTY;

        return fluidHandler.getFluidInTank(0);
    }

    // called only serverside
    public static void trySync(ItemStack stack, ServerPlayer player) {
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
    public static IFluidHandlerItem getFluidHandlerFromItem(ItemStack itemStack, Void v) {

        if (!Util.hasUUID(itemStack))
            return null;

        InsertMode insertMode = Util.getInsertMode(itemStack);
        int selectedslot = Util.getSelectedSlot(itemStack);
        TankInteractionMode interactionMode = Util.getInteractionMode(itemStack);

        boolean isClient = Thread.currentThread().getName().equals("Render thread");

        if (isClient) {
            CachedFluidStorageState cached = ClientTankCache.getOrQueueUpdate(Util.getUUID(itemStack));
            if (cached == null)
                return null;

            if (interactionMode == TankInteractionMode.BUCKET) {
                if (selectedslot == -1) {
                    return FilteringStorage.insertOnlyOf(cached.getFluidHandler(insertMode, ));
                } else {
                    selectedslot = Math.min(selectedslot, cached.getUniqueFluids().size() - 1);
                    FluidVariant selectedFluid = selectedslot == -1 ? FluidVariant.blank()
                            : cached.getUniqueFluids().get(selectedslot).fluidVariant();
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
        return getFluidHandlerFromItem(itemStack, ContainerItemContext.withConstant(itemStack));
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

            selectedslot = Math.min(selectedslot, cached.getUniqueFluids().size() - 1);
            return selectedslot == -1 ? null
                    : cached.getUniqueFluids().get(selectedslot).fluidVariant();

        } else {
            TankFluidStorageState tank = getFluidStorage(itemStack);

            List<FluidSlotData> fluids = tank.getUniqueFluids();
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

    public static void onToggleInteractionMode(PlayerEntity player, ItemStack stack) {

        TankOptions options = getOptionsOrDefault(stack);
        options.interactionMode = options.interactionMode.next();
        setOptions(stack, options);
        player.sendMessage(Text.translatable("popup.tankstorage.interactionmode."
                + options.interactionMode.toString().toLowerCase()), true);
    }

    @Nullable
    public static ItemStack getHeldTank(PlayerEntity player) {

        if (isTankLike(player.getMainHandStack()))
            return player.getMainHandStack();

        if (isTankLike(player.getOffHandStack()))
            return player.getOffHandStack();

        return null;
    }

    public static TankInteractionMode getInteractionMode(ItemStack stack) {
        return getOptionsOrDefault(stack).interactionMode;
    }
}
