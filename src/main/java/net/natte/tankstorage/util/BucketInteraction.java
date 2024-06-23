package net.natte.tankstorage.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.natte.tankstorage.item.TankFunctionality;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.storage.TankInteractionMode;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class BucketInteraction {

    // pass on miss or no uuid, success on place/pickup, fail otherwise
    public static InteractionResult interactFluid(Level world, Player player, InteractionHand hand, ItemStack stack) {
        if (!Util.hasUUID(stack))
            return InteractionResult.PASS;

        assert Util.getInteractionMode(stack) == TankInteractionMode.BUCKET
                : "cannot interact with fluids in world if not in bucket mode";

        TankFluidStorageState tankState = null;
        if (!world.isClientSide)
            tankState = Util.getFluidStorage(stack);

        IFluidHandlerItem fluidStorage = Util.getFluidStorageFromItem(stack);

        if (fluidStorage == null)
            return InteractionResult.FAIL;

        FluidStack selectedFluid = Util.getSelectedFluid(stack);

        if (selectedFluid == null) {
            InteractionResult result = BucketInteraction.pickUpFluid(world, player, stack);
            if (result.consumesAction() && !world.isClientSide) {
                tankState.sync((ServerPlayer) player);
                Util.clampSelectedSlotServer(stack);
            }
            return result;
        } else {
            InteractionResult result = BucketInteraction.placeFluid(selectedFluid, world,
                    player, hand, stack);
            if (result.consumesAction() && !world.isClientSide) {
                tankState.sync((ServerPlayer) player);
                Util.clampSelectedSlotServer(stack);
            }
            return result;
        }
    }

    // pass on miss, success on place, fail otherwise
    public static InteractionResult placeFluid(FluidStack fluidVariant, Level world,
                                               Player player, InteractionHand hand, ItemStack stack) {

        assert !fluidVariant.isEmpty() : "cannot place blank fluid";

        Fluid fluid = fluidVariant.getFluid();
        BlockHitResult hit = TankFunctionality.raycast(world, player, ClipContext.Fluid.NONE);

        if (hit.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }

        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.FAIL;
        }

        BlockPos blockPos = hit.getBlockPos();

        Direction direction = hit.getDirection();
        BlockPos blockPos2 = blockPos.relative(direction);

        BlockState blockState = world.getBlockState(blockPos);

        // TODO: respect direction? maybe use old code instead
        return FluidUtil.tryPlaceFluid(player, world, hand, blockPos, stack, fluidVariant).isSuccess() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
//
//        long extractedSimulated = StorageUtil.simulateExtract(fluidStorage, FluidVariant.of(fluid),
//                FluidConstants.BUCKET, null);
//        boolean canInsertFluid = extractedSimulated == FluidConstants.BUCKET;
//        if (!canInsertFluid) {
//            return InteractionResult.FAIL;
//        }
//
//        BlockPos blockPos3 = blockState.getBlock() instanceof FluidFillable && fluid == Fluids.WATER ? blockPos
//                : blockPos2;
//
//        if (!(fluidVariant.getFluid().getBucketItem() instanceof BucketItem bucketItem))
//            return InteractionResult.FAIL;
//
//        boolean didPlaceFluid = bucketItem.placeFluid(player, world, blockPos3, hit);
//
//        if (didPlaceFluid) {
//            try (Transaction transaction = Transaction.openOuter()) {
//                fluidStorage.extract(fluidVariant, FluidConstants.BUCKET, transaction);
//                transaction.commit();
//            }
//        }
//
//        return didPlaceFluid ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    // pass on miss, success on pickup, fail otherwise
    public static InteractionResult pickUpFluid(Level world, Player player,
                                                ItemStack stack) {

        BlockHitResult hit = TankFunctionality.raycast(world, player, ClipContext.Fluid.SOURCE_ONLY);

        if (hit.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }

        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.FAIL;
        }

        BlockPos blockPos = hit.getBlockPos();
        Direction direction = hit.getDirection();
        BlockPos blockPos2 = blockPos.relative(direction);
        if (!world.mayInteract(player, blockPos) || !player.mayUseItemAt(blockPos2, direction, stack)) {
            return InteractionResult.FAIL;
        }

        Fluid fluid = world.getFluidState(blockPos).getType();
        if (fluid == Fluids.EMPTY) {
            return InteractionResult.FAIL;
        }
        FluidActionResult fluidActionResult = FluidUtil.tryPickUpFluid(stack, player, world, blockPos, direction);
        return fluidActionResult.isSuccess() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
//        long insertedSimulated = StorageUtil.simulateInsert(fluidStorage, FluidVariant.of(fluid), FluidConstants.BUCKET,
//                null);
//        boolean canInsertFluid = insertedSimulated == FluidConstants.BUCKET;
//        if (!canInsertFluid) {
//            return InteractionResult.FAIL;
//        }
//
//        BlockState blockState = world.getBlockState(blockPos);
//        if (blockState.getBlock() instanceof FluidDrainable fluidDrainable
//                && !fluidDrainable.tryDrainFluid(world, blockPos, blockState).isEmpty()) {
//
//            try (Transaction transaction = Transaction.openOuter()) {
//                fluidStorage.insert(FluidVariant.of(fluid), FluidConstants.BUCKET, transaction);
//                transaction.commit();
//            }
//
//            fluidDrainable.getBucketFillSound().ifPresent(sound -> player.playSound((SoundEvent) sound, 1.0f, 1.0f));
//            world.emitGameEvent((Entity) player, GameEvent.FLUID_PICKUP, blockPos);
//
//            return InteractionResult.SUCCESS;
//        }
//
//        return InteractionResult.FAIL;
    }
}
