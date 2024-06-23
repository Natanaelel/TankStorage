package net.natte.tankstorage.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.cache.CachedFluidStorageState;
import net.natte.tankstorage.cache.ClientTankCache;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.item.tooltip.TankTooltipData;
import net.natte.tankstorage.screenhandler.TankScreenHandlerFactory;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.storage.TankInteractionMode;
import net.natte.tankstorage.util.BucketInteraction;
import net.natte.tankstorage.util.Util;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class TankFunctionality extends Item {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    public TankFunctionality(Properties settings) {
        super(settings);
    }

    // cases
    // sneaking hit bucket
    // 0 0 0 -> screen
    // 0 0 1 -> screen
    // 0 1 0 -> screen
    // 0 1 1 -> bucket
    // 1 0 0 -> toggle
    // 1 0 1 -> toggle
    // 1 1 0 -> toggle
    // 1 1 1 -> bucket
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        TankInteractionMode interactionMode = Util.getInteractionMode(stack);

        if (interactionMode == TankInteractionMode.BUCKET) {
            InteractionResult result = BucketInteraction.interactFluid(world, player, hand, stack);
            if (result == InteractionResult.PASS) {
                if (player.isShiftKeyDown()) {
                    if (!world.isClientSide)
                        Util.onToggleInteractionMode(player, stack);
                    return InteractionResultHolder.success(stack);
                } else {
                    return tryOpenScreen(world, player, stack);
                }
            } else {
                return result.consumesAction() ? InteractionResultHolder.success(stack) : InteractionResultHolder.fail(stack);
            }
        } else {
            if (player.isShiftKeyDown()) {
                if (!world.isClientSide)
                    Util.onToggleInteractionMode(player, stack);
                return InteractionResultHolder.success(stack);
            } else {
                return tryOpenScreen(world, player, stack);
            }
        }
    }

    private InteractionResultHolder<ItemStack> tryOpenScreen(Level world, Player player, ItemStack stack) {
        if (world.isClientSide)
            return InteractionResultHolder.success(stack);

        TankFluidStorageState tank = Util.getOrCreateFluidStorage(stack);
        if (tank == null) {
            player.displayClientMessage(Component.translatable("popup.tankstorage.unlinked"), true);
            return InteractionResultHolder.fail(stack);
        }
        TankScreenHandlerFactory screenHandlerFactory = new TankScreenHandlerFactory(tank, stack,
                player.getInventory().selected,
                ContainerLevelAccess.NULL);
        player.openMenu(screenHandlerFactory, screenHandlerFactory::writeScreenOpeningData);

        return InteractionResultHolder.success(stack);
    }

    // called clientside only
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!Util.hasUUID(stack))
            return Optional.empty();

        CachedFluidStorageState tank = ClientTankCache.getAndQueueThrottledUpdate(Util.getUUID(stack), 1 * 20);

        if (tank == null)
            return Optional.empty();

        TankInteractionMode interactionMode = Util.getInteractionMode(stack);

        int selectedSlot = interactionMode == TankInteractionMode.BUCKET ? Util.getSelectedSlot(stack) : -2;

        if (tank.getUniqueFluids().isEmpty() && selectedSlot != -1)
            return Optional.empty();

        return Optional.of(new TankTooltipData(tank.getUniqueFluids(), selectedSlot));
    }

    public static BlockHitResult raycast(Level world, Player player, ClipContext.Fluid fluidHandling) {
        return Item.getPlayerPOVHitResult(world, player, fluidHandling);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipType) {
        if (Util.isShiftDown.get()) {
            if (Util.hasUUID(stack))
                tooltip.add(Component.literal(Util.getUUID(stack).toString()).withStyle(ChatFormatting.DARK_AQUA));
        }

        TankType type = Util.getType(stack);
        if (type != null) {
            Component formattedSlotSize = Component.literal(NUMBER_FORMAT.format(type.getCapacity() / TankStorage.BUCKET));
            tooltip.add(Component.translatable("tooptip.tankstorage.slotsize", formattedSlotSize));
            tooltip.add(Component.translatable("tooptip.tankstorage.numslots", Component.literal(String.valueOf(type.size()))));
        }
        super.appendHoverText(stack, context, tooltip, tooltipType);
    }
}
