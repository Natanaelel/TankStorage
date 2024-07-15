package net.natte.tankstorage.util;

import com.google.common.base.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.natte.tankstorage.storage.TankFluidHandler;
import net.natte.tankstorage.storage.TankInteractionMode;
import net.natte.tankstorage.storage.TankOptions;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class Util {

    public static Supplier<Boolean> isShiftDown = () -> false;
    public static ThreadLocal<Boolean> isClient = ThreadLocal.withInitial(() -> false);

    public static ResourceLocation ID(String id) {
        return ResourceLocation.fromNamespaceAndPath(TankStorage.MOD_ID, id);
    }

    // call serverside only. returns null if unlinked linkitem
    @Nullable
    public static TankFluidStorageState getOrCreateFluidStorage(ItemStack tankItem) {
        if (isLink(tankItem))
            return hasUUID(tankItem) ? getFluidStorage(getUUID(tankItem)) : null;

        if (hasUUID(tankItem))
            return TankStateManager.getState().getOrCreate(getUUID(tankItem), getType(tankItem));
        else {
            UUID uuid = UUID.randomUUID();
            TankType type = getType(tankItem);
            tankItem.set(TankStorage.UUIDComponentType, uuid);
            return TankStateManager.getState().getOrCreate(uuid, type);
        }
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
        return tankItem.has(TankStorage.UUIDComponentType);
    }

    public static UUID getUUID(ItemStack tankItem) {
        return tankItem.get(TankStorage.UUIDComponentType);
    }

    public static TankOptions getOptionsOrDefault(ItemStack tankItem) {
        return tankItem.getOrDefault(TankStorage.OptionsComponentType, TankOptions.create());
    }

    public static void setOptions(ItemStack stack, TankOptions options) {
        stack.set(TankStorage.OptionsComponentType, options);
    }

    public static InsertMode getInsertMode(ItemStack tankItem) {
        return tankItem.getOrDefault(TankStorage.OptionsComponentType, TankOptions.create()).insertMode();
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
        clampSelectedSlot(stack, tank.getUniqueFluids().size() - 1);
    }

    // if in bucketmode: only allow extraction of selected fluid
    @Nullable
    public static IFluidHandlerItem getFluidHandlerFromItem(ItemStack itemStack, Void v) {
        return getFluidHandlerFromItem(itemStack);
    }

    @Nullable
    public static IFluidHandlerItem getFluidHandlerFromItem(ItemStack itemStack) {
        if (isClient())
            return getClientFluidHandler(itemStack);
        else
            return getServerFluidHandler(itemStack);
    }


    @Nullable
    private static IFluidHandlerItem getClientFluidHandler(ItemStack itemStack) {
        if (!hasUUID(itemStack))
            return null;
        CachedFluidStorageState tank = ClientTankCache.getAndQueueThrottledUpdate(getUUID(itemStack), 20);
        if (tank == null)
            return null;

        InsertMode insertMode = getInsertMode(itemStack);
        TankInteractionMode interactionMode = getInteractionMode(itemStack);

        TankFluidHandler fluidHandler = tank.getFluidHandler(insertMode).withItem(itemStack);

        if (interactionMode == TankInteractionMode.OPEN_SCREEN)
            return fluidHandler;

        FluidStack selectedFluid = tank.getSelectedFluid(getSelectedSlot(itemStack));
        if (selectedFluid == null)
            return fluidHandler.insertOnly();

        return fluidHandler.extractOnly(selectedFluid);
    }

    // does not create new tank
    @Nullable
    private static IFluidHandlerItem getServerFluidHandler(ItemStack itemStack) {
        if (!hasUUID(itemStack))
            return null;
        TankFluidStorageState tank = getFluidStorage(getUUID(itemStack));
        if (tank == null)
            return null;

        InsertMode insertMode = getInsertMode(itemStack);
        TankInteractionMode interactionMode = getInteractionMode(itemStack);

        TankFluidHandler fluidHandler = tank.getFluidHandler(insertMode).withItem(itemStack);

        if (interactionMode == TankInteractionMode.OPEN_SCREEN)
            return fluidHandler;

        FluidStack selectedFluid = tank.getSelectedFluid(getSelectedSlot(itemStack));
        if (selectedFluid == null)
            return fluidHandler.insertOnly();

        return fluidHandler.extractOnly(selectedFluid);
    }

    // never returns FluidStack.EMPTY but instead null
    @Nullable
    public static FluidStack getSelectedFluid(ItemStack itemStack) {

        if (!Util.hasUUID(itemStack))
            return null;

        int selectedslot = Util.getSelectedSlot(itemStack);

        if (isClient()) {
            CachedFluidStorageState cached = ClientTankCache.getOrQueueUpdate(Util.getUUID(itemStack));
            if (cached == null)
                return null;

            selectedslot = Math.min(selectedslot, cached.getUniqueFluids().size() - 1);
            return selectedslot == -1 ? null
                    : cached.getUniqueFluids().get(selectedslot).fluid();

        } else {
            TankFluidStorageState tank = getOrCreateFluidStorage(itemStack);
            if (tank == null)
                return null;

            List<LargeFluidSlotData> fluids = tank.getUniqueFluids();
            selectedslot = clampSelectedSlot(itemStack, fluids.size() - 1);
            return selectedslot == -1 ? null
                    : fluids.get(selectedslot).fluid();
        }
    }

    public static void clampSelectedSlotServer(ItemStack stack) {
        int max = getFluidStorage(stack).getNonEmptyFluidsSize() - 1;
        clampSelectedSlot(stack, max);
    }

    public static void onToggleInteractionMode(Player player, ItemStack stack) {

        stack.update(TankStorage.OptionsComponentType, TankOptions.create(), TankOptions::nextInteractionMode);
        TankInteractionMode interactionMode = getInteractionMode(stack);
        player.displayClientMessage(Texts.interactionMode(interactionMode), true);
    }

    @Nullable
    public static ItemStack getHeldTank(Player player) {

        if (isTankLike(player.getMainHandItem()))
            return player.getMainHandItem();

        if (isTankLike(player.getOffhandItem()))
            return player.getOffhandItem();

        return null;
    }

    public static TankInteractionMode getInteractionMode(ItemStack stack) {
        return getOptionsOrDefault(stack).interactionMode();
    }

    public static boolean isClient() {
        return isClient.get();
    }
}
