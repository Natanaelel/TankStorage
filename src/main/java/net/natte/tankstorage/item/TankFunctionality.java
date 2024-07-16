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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.natte.tankstorage.cache.CachedFluidStorageState;
import net.natte.tankstorage.cache.ClientTankCache;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.item.tooltip.TankTooltipData;
import net.natte.tankstorage.menu.TankMenuFactory;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.storage.TankInteractionMode;
import net.natte.tankstorage.util.BucketInteraction;
import net.natte.tankstorage.util.Texts;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class TankFunctionality extends Item {

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
        Supplier<InteractionResult> bucketInteractionResult = Lazy.of(() -> BucketInteraction.interactFluid(world, player, hand, stack));

        boolean shouldToggle = player.isShiftKeyDown() && (interactionMode == TankInteractionMode.OPEN_SCREEN || bucketInteractionResult.get() == InteractionResult.PASS);
        if (shouldToggle) {
            if (!world.isClientSide)
                Util.onToggleInteractionMode(player, stack);
            return InteractionResultHolder.success(stack);
        }

        boolean shouldOpenScreen = interactionMode == TankInteractionMode.OPEN_SCREEN || (bucketInteractionResult.get() == InteractionResult.PASS && !preventOpenScreenOnFluidClick(world, player));
        if (shouldOpenScreen) {
            return tryOpenScreen(world, player, stack);
        }
        return bucketInteractionResult.get().consumesAction() ? InteractionResultHolder.success(stack) : InteractionResultHolder.fail(stack);

//        return
//        System.out.println(shouldOpenScreen);

//        if (interactionMode == TankInteractionMode.BUCKET) {
//            InteractionResult result = BucketInteraction.interactFluid(world, player, hand, stack);
//            if (result == InteractionResult.PASS) {
//
//                // allow player to right-click flowing fluid without accidentally opening screen
//
//                if (player.isShiftKeyDown()) {
//                    if (!world.isClientSide)
//                        Util.onToggleInteractionMode(player, stack);
//                    return InteractionResultHolder.success(stack);
//                } else if (preventOpenScreenOnFluidClick(world, player))
//                    return InteractionResultHolder.fail(stack);
//                else {
//                    return tryOpenScreen(world, player, stack);
//                }
//            } else {
//                return result.consumesAction() ? InteractionResultHolder.success(stack) : InteractionResultHolder.fail(stack);
//            }
//        } else {
//            if (player.isShiftKeyDown()) {
//                if (!world.isClientSide)
//                    Util.onToggleInteractionMode(player, stack);
//                return InteractionResultHolder.success(stack);
//            } else {
//                return tryOpenScreen(world, player, stack);
//            }
//        }
    }

    private boolean preventOpenScreenOnFluidClick(Level level, Player player) {
        Vec3 vec3 = player.getEyePosition();
        Vec3 vec31 = vec3.add(player.calculateViewVector(player.getXRot(), player.getYRot()).scale(player.blockInteractionRange() + 1));
        return level.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player)).getType() != HitResult.Type.MISS;
    }

    private InteractionResultHolder<ItemStack> tryOpenScreen(Level world, Player player, ItemStack stack) {
        if (world.isClientSide)
            return InteractionResultHolder.success(stack);

        TankFluidStorageState tank = Util.getOrCreateFluidStorage(stack);
        if (tank == null) {
            player.displayClientMessage(Texts.UNLINKED, true);
            return InteractionResultHolder.fail(stack);
        }
        TankMenuFactory menu = new TankMenuFactory(tank, stack,
                player.getInventory().selected,
                ContainerLevelAccess.NULL);
        menu.open(player);

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
            tooltip.add(Texts.slotSizeTooltip(type.getCapacity()));
            tooltip.add(Texts.slotCountTooltip(type.size()));
        }
        super.appendHoverText(stack, context, tooltip, tooltipType);
    }
}
