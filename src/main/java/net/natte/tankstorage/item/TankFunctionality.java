package net.natte.tankstorage.item;

import java.util.List;
import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.cache.CachedFluidStorageState;
import net.natte.tankstorage.cache.ClientTankCache;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.item.tooltip.TankTooltipData;
import net.natte.tankstorage.screenhandler.TankScreenHandlerFactory;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.state.TankStateManager;
import net.natte.tankstorage.storage.InsertMode;
import net.natte.tankstorage.storage.TankInteractionMode;
import net.natte.tankstorage.storage.TankOptions;
import net.natte.tankstorage.storage.TankSingleFluidStorage;
import net.natte.tankstorage.util.FluidSlotData;
import net.natte.tankstorage.util.Util;

public class TankFunctionality extends Item {

    public TankFunctionality(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        // if (world.isClient)
        //     return TypedActionResult.pass(stack);

        if (player.isSneaking()) {
            TankOptions options = Util.getOptionsOrDefault(stack);
            options.interactionMode = options.interactionMode == TankInteractionMode.BUCKET
                    ? TankInteractionMode.OPEN_SCREEN
                    : TankInteractionMode.BUCKET;
            Util.setOptions(stack, options);
            player.sendMessage(Text.of("mode is now " + options.interactionMode));
            return TypedActionResult.success(stack);
        }

        TankOptions options = Util.getOptionsOrDefault(stack);
        if (options.interactionMode == TankInteractionMode.BUCKET) {
            if (interactFluid(world, player, stack))
                return TypedActionResult.success(stack);
            return TypedActionResult.fail(stack);

        }

        TankFluidStorageState tank = Util.getOrCreateFluidStorage(stack);
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

    private boolean interactFluid(World world, PlayerEntity player, ItemStack stack) {
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
            return pickUpFluid(tankState, world, player, stack);
        } else {
            return placeFluid(fluids.get(options.selectedSlot).fluidVariant(), tankState, world, player, stack);
        }

    

    }

    private boolean placeFluid(FluidVariant fluidVariant, TankFluidStorageState tankState, World world,
            PlayerEntity player, ItemStack stack) {
        return false;
    }

    private boolean pickUpFluid(TankFluidStorageState tankState, World world, PlayerEntity player, ItemStack stack) {
        player.sendMessage(Text.of("try to pick up fluid"));
        Storage<FluidVariant> fluidStorage = tankState.getFluidStorage(Util.getInsertMode(stack));

        BlockHitResult hit = Item.raycast(world, player, FluidHandling.SOURCE_ONLY);

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
        if (fluid == Fluids.EMPTY){
            player.sendMessage(Text.of("empty fluid"));            
            return false;
        }
        long insertedSimulated = StorageUtil.simulateInsert(fluidStorage, FluidVariant.of(fluid), FluidConstants.BUCKET,
                null);
                player.sendMessage(Text.of(""+insertedSimulated));
        boolean canInsertFluid = insertedSimulated == FluidConstants.BUCKET;
        if (!canInsertFluid){
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
            if(!world.isClient)
                tankState.sync((ServerPlayerEntity)player);

            return true;
        }

        return false;
    }
}
