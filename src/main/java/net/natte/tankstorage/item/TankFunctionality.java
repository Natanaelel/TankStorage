package net.natte.tankstorage.item;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext;
import net.natte.tankstorage.cache.CachedFluidStorageState;
import net.natte.tankstorage.cache.ClientTankCache;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.item.tooltip.TankTooltipData;
import net.natte.tankstorage.screenhandler.TankScreenHandlerFactory;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.storage.TankInteractionMode;
import net.natte.tankstorage.storage.TankOptions;
import net.natte.tankstorage.util.BucketInteraction;
import net.natte.tankstorage.util.Util;

public class TankFunctionality extends Item {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    public TankFunctionality(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        // TODO: open, toggle mode, bucket
        // if (world.isClient)
        // return TypedActionResult.pass(stack);

        if (player.isSneaking()) {
            if (!world.isClient)
                Util.onToggleInteractionMode(player, stack);
            return TypedActionResult.success(stack);
        }

        TankOptions options = Util.getOptionsOrDefault(stack);
        if (options.interactionMode == TankInteractionMode.BUCKET) {
            if (BucketInteraction.interactFluid(world, player, stack))
                return TypedActionResult.success(stack);
            return TypedActionResult.fail(stack);

        }
        if (player.getWorld().isClient())
            return TypedActionResult.success(stack);

        TankFluidStorageState tank = Util.getOrCreateFluidStorage(stack);
        if (tank == null) {
            player.sendMessage(Text.translatable("popup.tankstorage.unlinked"), true);
            return TypedActionResult.fail(stack);
        }
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

        CachedFluidStorageState tank = ClientTankCache.getAndQueueThrottledUpdate(Util.getUUID(stack), 1 * 20);

        if (tank == null)
            return Optional.empty();

        if (tank.getNonEmptyFluids().isEmpty())
            return Optional.empty();
        TankInteractionMode interactionMode = Util.getInteractionMode(stack);

        int selectedSlot = interactionMode == TankInteractionMode.BUCKET ? Util.getSelectedSlot(stack) : -1;
        return Optional.of(new TankTooltipData(tank.getNonEmptyFluids(), selectedSlot));
    }

    public static BlockHitResult raycast(World world, PlayerEntity player, RaycastContext.FluidHandling fluidHandling) {
        return Item.raycast(world, player, fluidHandling);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        if (Util.isShiftDown.get()) {
            if (Util.hasUUID(stack))
                tooltip.add(Text.literal(Util.getUUID(stack).toString()).formatted(Formatting.DARK_AQUA));
        }

        TankType type = Util.getType(stack);
        if (type != null) {
            Text formattedSlotSize = Text.literal(NUMBER_FORMAT.format(type.getCapacity() / FluidConstants.BUCKET));
            tooltip.add(Text.translatable("tooptip.tankstorage.slotsize", formattedSlotSize));
            tooltip.add(Text.translatable("tooptip.tankstorage.numslots", Text.literal(String.valueOf(type.size()))));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}
