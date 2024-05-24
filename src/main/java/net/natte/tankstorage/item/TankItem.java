package net.natte.tankstorage.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.util.Util;

public class TankItem extends Item {

    public final TankType type;

    public TankItem(Settings settings, TankType type) {
        super(settings);
        this.type = type;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (world.isClient)
            return TypedActionResult.pass(stack);

        TankFluidStorageState tank = Util.getOrCreateFluidStorage(stack, world);
        player.openHandledScreen(tank.getScreenHandlerFactory(stack));
        return TypedActionResult.success(stack);
    }

}
