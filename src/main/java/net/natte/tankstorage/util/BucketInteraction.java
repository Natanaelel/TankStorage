package net.natte.tankstorage.util;

import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.cache.ClientTankCache;
import net.natte.tankstorage.item.TankFunctionality;
import net.natte.tankstorage.item.TankItem;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.storage.TankOptions;

public class BucketInteraction {

     public static boolean interactFluid(World world, PlayerEntity player, ItemStack stack) {
        if (!Util.hasUUID(stack))
            return false;

        TankFluidStorageState tankState;
        if (world.isClient) {
            var cached = ClientTankCache.get(Util.getUUID(stack));
            if (cached == null){
                tankState = TankFluidStorageState.create(TankStorage.TANK_TYPES[6], Util.getUUID(stack));
            }else{
                var nbt = new NbtCompound();

        nbt.putUuid("uuid", Util.getUUID(stack));
        nbt.putString("type", ((TankItem)stack.getItem()).type.getName());
        nbt.putShort("revision", (short)0);

        NbtList fluids = new NbtList();
                
        for (var f : cached.getFluids()) {
            NbtCompound fluidNbt = new NbtCompound();
            fluidNbt.put("variant", f.fluidVariant().toNbt());
            fluidNbt.putLong("amount", f.amount());
            fluidNbt.putBoolean("locked", f.isLocked());
            fluids.add(fluidNbt);
        }

        nbt.put("fluids", fluids);

                tankState = TankFluidStorageState.readNbt(nbt);
            }
        } else {
            tankState = Util.getFluidStorage(stack);
        }

        TankOptions options = Util.getOptionsOrDefault(stack);

        List<FluidSlotData> fluids = tankState.getFluidSlotDatas();
        options.selectedSlot = MathHelper.clamp(options.selectedSlot, -1, fluids.size() - 1);
        Util.setOptions(stack, options);

        if (options.selectedSlot == -1) {
            return BucketInteraction.pickUpFluid(tankState, world, player, stack);
        } else {
            return BucketInteraction.placeFluid(fluids.get(options.selectedSlot).fluidVariant(), tankState, world, player, stack);
        }
        
    }
    
    public static boolean placeFluid(FluidVariant fluidVariant, TankFluidStorageState tankState, World world,
            PlayerEntity player, ItemStack stack) {
        return false;
    }

    public static boolean pickUpFluid(TankFluidStorageState tankState, World world, PlayerEntity player,
            ItemStack stack) {
        player.sendMessage(Text.of("try to pick up fluid"));
        Storage<FluidVariant> fluidStorage = tankState.getFluidStorage(Util.getInsertMode(stack));

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
        player.sendMessage(Text.of("" + insertedSimulated));
        boolean canInsertFluid = insertedSimulated == FluidConstants.BUCKET;
        if (!canInsertFluid) {
            player.sendMessage(Text.of("simulation failed on " + (world.isClient ? "client" : "server")));
            return false;
        }

        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock() instanceof FluidDrainable fluidDrainable
                && !fluidDrainable.tryDrainFluid(world, blockPos, blockState).isEmpty()) {
            fluidDrainable.getBucketFillSound().ifPresent(sound -> player.playSound((SoundEvent) sound, 1.0f, 1.0f));
            // TODO: proper sound? maybe world.playsound or something
            world.emitGameEvent((Entity) player, GameEvent.FLUID_PICKUP, blockPos);
            try (Transaction transaction = Transaction.openOuter()) {
                fluidStorage.insert(FluidVariant.of(fluid), FluidConstants.BUCKET, transaction);
                transaction.commit();
            }
            if (!world.isClient)
                tankState.sync((ServerPlayerEntity) player);

            return true;
        }

        return false;
    }
}
