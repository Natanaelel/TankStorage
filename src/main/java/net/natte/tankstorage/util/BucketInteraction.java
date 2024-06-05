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
import net.minecraft.text.Text;
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

    public static boolean interactFluid(World world, PlayerEntity player, ItemStack stack) {
        if (!Util.hasUUID(stack))
            return false;

        assert Util.getOptionsOrDefault(stack).interactionMode == TankInteractionMode.BUCKET
                : "cannot interact with fluids in world if not in bucket mode";

        TankFluidStorageState tankState = null;
        if (!world.isClient)
            tankState = Util.getFluidStorage(stack);

        Storage<FluidVariant> fluidStorage = Util.getFluidStorageFromItem(stack);

        FluidVariant selectedFluid = Util.getSelectedFluid(stack);

        if (selectedFluid == null) {

            if (world.isClient)
                player.sendMessage(Text.of("client null? " + (fluidStorage == null)));

            boolean result = BucketInteraction.pickUpFluid(fluidStorage, world, player, stack);
            if (result && !world.isClient) {
                tankState.sync((ServerPlayerEntity) player);
                Util.clampSelectedSlotServer(stack);
            }
            return result;
        } else {
            if (world.isClient)
                player.sendMessage(Text.of("client null? " + (fluidStorage == null)));
            boolean result = BucketInteraction.placeFluid(selectedFluid, fluidStorage, world,
                    player, stack);
            if (result && !world.isClient) {
                tankState.sync((ServerPlayerEntity) player);
                Util.clampSelectedSlotServer(stack);
            }
            return result;
        }

    }

    public static boolean placeFluid(FluidVariant fluidVariant, Storage<FluidVariant> fluidStorage, World world,
            PlayerEntity player, ItemStack stack) {
        player.sendMessage(Text.of("try to place fluid"));

        assert !fluidVariant.isBlank() : "cannot place blank fluid";

        Fluid fluid = fluidVariant.getFluid();
        BlockHitResult hit = TankFunctionality.raycast(world, player, RaycastContext.FluidHandling.NONE);

        if (hit.getType() == Type.MISS) {
            player.sendMessage(Text.of("miss"));
            return false;
        }

        if (hit.getType() != Type.BLOCK) {
            player.sendMessage(Text.of("not block"));
            return false;
        }
        /* BucketItem */

        BlockPos blockPos = hit.getBlockPos();

        Direction direction = hit.getSide();
        BlockPos blockPos2 = blockPos.offset(direction);

        BlockState blockState = world.getBlockState(blockPos);

        long extractedSimulated = StorageUtil.simulateExtract(fluidStorage, FluidVariant.of(fluid),
                FluidConstants.BUCKET, null);
        player.sendMessage(Text.of("" + extractedSimulated));
        boolean canInsertFluid = extractedSimulated == FluidConstants.BUCKET;
        if (!canInsertFluid) {
            player.sendMessage(Text.of("simulation failed on " + (world.isClient ? "client" : "server")));
            return false;
        }

        BlockPos blockPos3 = blockState.getBlock() instanceof FluidFillable && fluid == Fluids.WATER ? blockPos
                : blockPos2;

        if (!(fluidVariant.getFluid().getBucketItem() instanceof BucketItem bucketItem))
            return false;

        boolean didPlaceFluid = bucketItem.placeFluid(player, world, blockPos3, hit);

        if (didPlaceFluid) {
            try (Transaction transaction = Transaction.openOuter()) {
                fluidStorage.extract(fluidVariant, FluidConstants.BUCKET, transaction);
                transaction.commit();
            }
        }

        return didPlaceFluid;

    }

    public static boolean pickUpFluid(Storage<FluidVariant> fluidStorage, World world, PlayerEntity player,
            ItemStack stack) {
        player.sendMessage(Text.of("try to pick up fluid"));
        // Storage<FluidVariant> fluidStorage =
        // tankState.getFluidStorage(Util.getInsertMode(stack));

        BlockHitResult hit = TankFunctionality.raycast(world, player, FluidHandling.SOURCE_ONLY);

        if (hit.getType() == Type.MISS) {
            player.sendMessage(Text.of("miss"));
            return false;
        }

        if (hit.getType() != Type.BLOCK) {
            player.sendMessage(Text.of("not block"));
            return false;
        }

        BlockPos blockPos = hit.getBlockPos();
        Direction direction = hit.getSide();
        BlockPos blockPos2 = blockPos.offset(direction);
        if (!world.canPlayerModifyAt(player, blockPos) || !player.canPlaceOn(blockPos2, direction, stack)) {
            player.sendMessage(Text.of("cannot modify or place"));
            return false;
        }

        Fluid fluid = world.getFluidState(blockPos).getFluid();
        if (fluid == Fluids.EMPTY) {
            player.sendMessage(Text.of("empty fluid"));
            return false;
        }
        long insertedSimulated = StorageUtil.simulateInsert(fluidStorage, FluidVariant.of(fluid), FluidConstants.BUCKET,
                null);
        // player.sendMessage(Text.of("" + insertedSimulated));
        player.sendMessage(Text.of(fluid + " " + insertedSimulated));
        boolean canInsertFluid = insertedSimulated == FluidConstants.BUCKET;
        if (!canInsertFluid) {
            player.sendMessage(Text.of("simulation failed on " + (world.isClient ? "client" : "server")));
            return false;
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

            return true;
        }

        return false;
    }
}
