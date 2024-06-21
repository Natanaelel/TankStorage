package net.natte.tankstorage.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.screenhandler.TankScreenHandler;
import net.natte.tankstorage.storage.TankOptions;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleInsertModePacketC2S() implements CustomPacketPayload {

    public static final ToggleInsertModePacketC2S INSTANCE = new ToggleInsertModePacketC2S();

    public static final Type<ToggleInsertModePacketC2S> TYPE = new Type<>(Util.ID("toggleinsertmode_c2s"));
    public static final StreamCodec<ByteBuf, ToggleInsertModePacketC2S> STREAM_CODEC = StreamCodec.unit(INSTANCE);


    @Override
    public Type<ToggleInsertModePacketC2S> type() {
        return TYPE;
    }

    public static void receive(ToggleInsertModePacketC2S packet, IPayloadContext context) {

        ServerPlayer player = ((ServerPlayer) context.player());
        if (player.containerMenu instanceof TankScreenHandler tankScreenHandler)
            toggleInsertModeOfScreenHandler(player, tankScreenHandler);
        else
            toggleInsertModeOfHeldTank(player);
    }

    private static void toggleInsertModeOfScreenHandler(ServerPlayer player,
                                                        TankScreenHandler tankScreenHandler) {
        ItemStack tank = tankScreenHandler.getTankItem();
        TankOptions options = Util.getOrCreateOptions(tank);
        options.insertMode = options.insertMode.next();
        Util.setOptions(tank, options);

        // dock.markDirty if has dock pos
        tankScreenHandler.getContext().execute(
                (world, blockPos) -> world
                        .getBlockEntity(blockPos, TankStorage.TANK_DOCK_BLOCK_ENTITY.get())
                        .ifPresent(dock -> {
                            if (dock.hasTank()) {
                                Util.setOptions(dock.getTank(), options);
                                dock.markDirty();
                            }
                        }));
    }

    private static void toggleInsertModeOfHeldTank(ServerPlayer player) {
        ItemStack stack;
        if (Util.isTankLike(player.getMainHandItem()))
            stack = player.getMainHandItem();
        else if (Util.isTankLike(player.getOffhandItem()))
            stack = player.getOffhandItem();
        else
            return;

        TankOptions options = Util.getOrCreateOptions(stack);
        options.insertMode = options.insertMode.next();
        Util.setOptions(stack, options);
        player.displayClientMessage(Component.translatable("popup.tankstorage.insertmode."
                + options.insertMode.toString().toLowerCase()), true);
    }
}
