package net.natte.tankstorage.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.natte.tankstorage.screenhandler.TankScreenHandlerFactory;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.util.Util;

public class TankDockBlock extends Block implements BlockEntityProvider {

    public TankDockBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
            BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof TankDockBlockEntity tankDock))
            return ActionResult.PASS;

        ItemStack stackInHand = player.getStackInHand(hand);
        if (tankDock.hasTank()) {

            // pick up tank from dock
            if (stackInHand.isEmpty() && player.isSneaking()) {
                ItemStack tankInDock = tankDock.pickUpTank();
                tankInDock.setBobbingAnimationTime(5);
                player.setStackInHand(hand, tankInDock);

                world.playSoundFromEntity(null, player, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f,
                        (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 1.4f + 2.0f);
                return ActionResult.FAIL;
            }

            // swap hand and dock
            if (Util.isTankLike(stackInHand)) {
                player.setStackInHand(hand, ItemStack.EMPTY);
                ItemStack tankInDock = tankDock.pickUpTank();
                tankInDock.setBobbingAnimationTime(5);
                player.setStackInHand(hand, tankInDock);
                world.playSound(null, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f,
                        SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.2f, 0.0f, 0);
                world.playSoundFromEntity(null, player, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f,
                        (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 1.4f + 2.0f);

                tankDock.putTank(stackInHand);
                return ActionResult.SUCCESS;
            }

            // open tank screen
            if (!world.isClient) {
                TankFluidStorageState tank = Util.getOrCreateFluidStorage(tankDock.getTank());
                if (tank == null) {
                    player.sendMessage(Text.translatable("popup.tankstorage.unlinked"), true);
                    return ActionResult.FAIL;
                }
                NamedScreenHandlerFactory screenHandlerFactory = new TankScreenHandlerFactory(
                        tank,
                        tankDock.getTank(),
                        -1,
                        ScreenHandlerContext.create(world, pos));
                player.openHandledScreen(screenHandlerFactory);
            }

            return ActionResult.SUCCESS;
        } else {
            // place tank in dock
            if (Util.isTankLike(stackInHand)) {
                tankDock.putTank(player.getStackInHand(hand));
                player.setStackInHand(hand, ItemStack.EMPTY);
                world.playSound(null, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f,
                        SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.2f, 0.0f, 0);

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;

    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TankDockBlockEntity(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) {
            return;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof TankDockBlockEntity tankDockBlockEntity) {
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), tankDockBlockEntity.getTank());
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

}
