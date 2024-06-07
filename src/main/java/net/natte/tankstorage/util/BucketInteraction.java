package net.natte.tankstorage.util;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.FluidFillable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.natte.tankstorage.item.TankFunctionality;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.storage.TankInteractionMode;

public class BucketInteraction {

    // pass on miss or no uuid, success on place/pickup, fail otherwise
    public static ActionResult interactFluid(World world, PlayerEntity player, ItemStack stack) {
        if (!Util.hasUUID(stack))
            return ActionResult.PASS;

        assert Util.getInteractionMode(stack) == TankInteractionMode.BUCKET
                : "cannot interact with fluids in world if not in bucket mode";

        TankFluidStorageState tankState = null;
        if (!world.isClient)
            tankState = Util.getFluidStorage(stack);

        Storage<FluidVariant> fluidStorage = Util.getFluidStorageFromItem(stack);

        FluidVariant selectedFluid = Util.getSelectedFluid(stack);

        if (selectedFluid == null) {
            ActionResult result = BucketInteraction.pickUpFluid(fluidStorage, world, player, stack);
            if (result.isAccepted() && !world.isClient) {
                tankState.sync((ServerPlayerEntity) player);
                Util.clampSelectedSlotServer(stack);
            }
            return result;
        } else {
            ActionResult result = BucketInteraction.placeFluid(selectedFluid, fluidStorage, world,
                    player, stack);
            if (result.isAccepted() && !world.isClient) {
                tankState.sync((ServerPlayerEntity) player);
                Util.clampSelectedSlotServer(stack);
            }
            return result;
        }
    }

    // pass on miss, success on place, fail otherwise
    public static ActionResult placeFluid(FluidVariant fluidVariant, Storage<FluidVariant> fluidStorage, World world,
            PlayerEntity player, ItemStack stack) {

        assert !fluidVariant.isBlank() : "cannot place blank fluid";

        Fluid fluid = fluidVariant.getFluid();
        BlockHitResult hit = TankFunctionality.raycast(world, player, RaycastContext.FluidHandling.NONE);

        if (hit.getType() == Type.MISS) {
            return ActionResult.PASS;
        }

        if (hit.getType() != Type.BLOCK) {
            return ActionResult.FAIL;
        }

        BlockPos blockPos = hit.getBlockPos();

        Direction direction = hit.getSide();
        BlockPos blockPos2 = blockPos.offset(direction);

        BlockState blockState = world.getBlockState(blockPos);

        long extractedSimulated = StorageUtil.simulateExtract(fluidStorage, FluidVariant.of(fluid),
                FluidConstants.BUCKET, null);
        boolean canInsertFluid = extractedSimulated == FluidConstants.BUCKET;
        if (!canInsertFluid) {
            return ActionResult.FAIL;
        }

        BlockPos blockPos3 = blockState.getBlock() instanceof FluidFillable && fluid == Fluids.WATER ? blockPos
                : blockPos2;

        if (!(fluidVariant.getFluid().getBucketItem() instanceof BucketItem bucketItem))
            return ActionResult.FAIL;

        boolean didPlaceFluid = bucketItem.placeFluid(player, world, blockPos3, hit);

        if (didPlaceFluid) {
            try (Transaction transaction = Transaction.openOuter()) {
                fluidStorage.extract(fluidVariant, FluidConstants.BUCKET, transaction);
                transaction.commit();
            }
        }

        return didPlaceFluid ? ActionResult.SUCCESS : ActionResult.FAIL;
    }

    // pass on miss, success on pickup, fail otherwise
    public static ActionResult pickUpFluid(Storage<FluidVariant> fluidStorage, World world, PlayerEntity player,
            ItemStack stack) {

        BlockHitResult hit = TankFunctionality.raycast(world, player, FluidHandling.SOURCE_ONLY);

        if (hit.getType() == Type.MISS) {
            return ActionResult.PASS;
        }

        if (hit.getType() != Type.BLOCK) {
            return ActionResult.FAIL;
        }

        BlockPos blockPos = hit.getBlockPos();
        Direction direction = hit.getSide();
        BlockPos blockPos2 = blockPos.offset(direction);
        if (!world.canPlayerModifyAt(player, blockPos) || !player.canPlaceOn(blockPos2, direction, stack)) {
            return ActionResult.FAIL;
        }

        Fluid fluid = world.getFluidState(blockPos).getFluid();
        if (fluid == Fluids.EMPTY) {
            return ActionResult.FAIL;
        }
        long insertedSimulated = StorageUtil.simulateInsert(fluidStorage, FluidVariant.of(fluid), FluidConstants.BUCKET,
                null);
        boolean canInsertFluid = insertedSimulated == FluidConstants.BUCKET;
        if (!canInsertFluid) {
            return ActionResult.FAIL;
        }

        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock() instanceof FluidDrainable fluidDrainable
                && !fluidDrainable.tryDrainFluid(world, blockPos, blockState).isEmpty()) {

            try (Transaction transaction = Transaction.openOuter()) {
                fluidStorage.insert(FluidVariant.of(fluid), FluidConstants.BUCKET, transaction);
                transaction.commit();
            }

            fluidDrainable.getBucketFillSound().ifPresent(sound -> player.playSound((SoundEvent) sound, 1.0f, 1.0f));
            world.emitGameEvent((Entity) player, GameEvent.FLUID_PICKUP, blockPos);

            return ActionResult.SUCCESS;
        }

        return ActionResult.FAIL;
    }
}
