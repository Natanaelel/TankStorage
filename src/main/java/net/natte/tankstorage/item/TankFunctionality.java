package net.natte.tankstorage.item;

import java.util.Optional;

import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.natte.tankstorage.cache.CachedFluidStorageState;
import net.natte.tankstorage.cache.ClientTankCache;
import net.natte.tankstorage.item.tooltip.TankTooltipData;
import net.natte.tankstorage.screenhandler.TankScreenHandlerFactory;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.util.Util;

public class TankFunctionality extends Item {

    public TankFunctionality(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (world.isClient)
            return TypedActionResult.pass(stack);

        TankFluidStorageState tank = Util.getOrCreateFluidStorage(stack, world);
        NamedScreenHandlerFactory screenHandlerFactory = new TankScreenHandlerFactory(tank, stack,
                player.getInventory().selectedSlot,
                ScreenHandlerContext.EMPTY);
        player.openHandledScreen(screenHandlerFactory);

        return TypedActionResult.success(stack);
    }

    // called clientside only
    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        if (!Util.hasUUID(stack))
            return Optional.empty();

        CachedFluidStorageState tank = ClientTankCache.getAndQueueThrottledUpdate(Util.getUUID(stack), 2 * 20);

        if (tank == null)
            return Optional.empty();

        if (tank.getFluids().isEmpty())
            return Optional.empty();

        return Optional.of(new TankTooltipData(tank.getFluids()));
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        // TODO: server cache for this (lame)
        // set owner so we can get a world reference from stack in FluidStorage.ITEM
        // lookup
        stack.setHolder(player);
        return super.onStackClicked(stack, slot, clickType, player);
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player,
            StackReference cursorStackReference) {

        // TODO: server cache for this (lame)
        stack.setHolder(player);
        return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

        super.inventoryTick(stack, world, entity, slot, selected);
    }

}
