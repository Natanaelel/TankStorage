package net.natte.tankstorage.packet.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.block.TankDockBlockEntity;
import net.natte.tankstorage.menu.TankMenu;
import net.natte.tankstorage.storage.TankOptions;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateTankOptionsPacketC2S(TankOptions options, boolean ofScreen) implements CustomPacketPayload {

    public static final Type<UpdateTankOptionsPacketC2S> TYPE = new Type<>(Util.ID("options_c2s"));
    public static final StreamCodec<FriendlyByteBuf, UpdateTankOptionsPacketC2S> STREAM_CODEC = StreamCodec.composite(
            TankOptions.STREAM_CODEC,
            UpdateTankOptionsPacketC2S::options,
            ByteBufCodecs.BOOL,
            UpdateTankOptionsPacketC2S::ofScreen,
            UpdateTankOptionsPacketC2S::new);

    @Override
    public Type<UpdateTankOptionsPacketC2S> type() {
        return TYPE;
    }

    public static void receive(UpdateTankOptionsPacketC2S packet, IPayloadContext context) {
        Player player = context.player();
        if (packet.ofScreen)
            updateOptionsOfScreen(packet.options, player.containerMenu);
        else
            updateOptionsOfHeldItem(packet.options, player);
    }


    private static void updateOptionsOfScreen(TankOptions options, AbstractContainerMenu menu) {
        if (!(menu instanceof TankMenu tankMenu))
            return;

        ItemStack tank = tankMenu.getTankItem();
        tank.set(TankStorage.OptionsComponentType, options);

        tankMenu.getAccess().execute(
                (world, blockPos) -> world
                        .getBlockEntity(blockPos, TankStorage.TANK_DOCK_BLOCK_ENTITY.get())
                        .ifPresent(TankDockBlockEntity::setChanged));
    }

    private static void updateOptionsOfHeldItem(TankOptions options, Player player) {
        ItemStack tank = Util.getHeldTank(player);
        if (tank == null)
            return;
        tank.set(TankStorage.OptionsComponentType, options);
    }
}
