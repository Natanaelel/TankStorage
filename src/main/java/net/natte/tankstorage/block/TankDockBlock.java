package net.natte.tankstorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.natte.tankstorage.menu.TankMenuFactory;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.util.Texts;
import net.natte.tankstorage.util.Util;

public class TankDockBlock extends Block implements EntityBlock {

    public TankDockBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof TankDockBlockEntity tankDock))
            return InteractionResult.PASS;

        ItemStack stackInHand = player.getMainHandItem();
        if (tankDock.hasTank()) {

            // pick up tank from dock
            if (stackInHand.isEmpty() && player.isShiftKeyDown()) {
                ItemStack tankInDock = tankDock.pickUpTank();
                tankInDock.setPopTime(5);
                player.setItemInHand(InteractionHand.MAIN_HAND, tankInDock.copyAndClear());

                world.playSound(null, player, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f,
                        (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 1.4f + 2.0f);
                return InteractionResult.FAIL;
            }

            // swap hand and dock
            if (Util.isTankLike(stackInHand)) {
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                ItemStack tankInDock = tankDock.pickUpTank();
                tankInDock.setPopTime(5);
                player.setItemInHand(InteractionHand.MAIN_HAND, tankInDock);
                world.playSound(null, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f,
                        SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f, 0.0f);
                world.playSound(null, player, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f,
                        (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 1.4f + 2.0f);

                tankDock.putTank(stackInHand);
                return InteractionResult.SUCCESS;
            }

            // open tank screen
            if (!world.isClientSide) {
                TankFluidStorageState tank = Util.getOrCreateFluidStorage(tankDock.getTank());
                if (tank == null) {
                    player.displayClientMessage(Texts.UNLINKED, true);
                    return InteractionResult.FAIL;
                }
                TankMenuFactory menu = new TankMenuFactory(
                        tank,
                        tankDock.getTank(),
                        -1,
                        ContainerLevelAccess.create(world, pos));
                menu.open(player);
            }

            return InteractionResult.SUCCESS;
        } else {
            // place tank in dock
            if (Util.isTankLike(stackInHand)) {
                tankDock.putTank(player.getMainHandItem());
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                world.playSound(null, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f,
                        SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f, 0.0f);

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TankDockBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.is(newState.getBlock())) {
            return;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof TankDockBlockEntity tankDockBlockEntity) {
            Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), tankDockBlockEntity.getTank());
        }
        super.onRemove(state, world, pos, newState, moved);
    }
}
