package net.natte.tankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.screenhandler.TankScreenHandler;
import net.natte.tankstorage.storage.TankOptions;
import net.natte.tankstorage.util.Util;

public record ToggleInsertModePacketC2S() implements FabricPacket {

    public static final PacketType<ToggleInsertModePacketC2S> PACKET_TYPE = PacketType.create(Util.ID("toggleinsertmode_c2s"),
            ToggleInsertModePacketC2S::read);

    public static ToggleInsertModePacketC2S read(PacketByteBuf buf) {
        return new ToggleInsertModePacketC2S();
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    @Override
    public PacketType<ToggleInsertModePacketC2S> getType() {
        return PACKET_TYPE;
    }

    public static void receive(ToggleInsertModePacketC2S packet, ServerPlayerEntity player, PacketSender responseSender) {

        if (player.currentScreenHandler instanceof TankScreenHandler tankScreenHandler)
            toggleInsertModeOfScreenHandler(player, tankScreenHandler);
        else
            toggleInsertModeOfHeldTank(player);
    }

    private static void toggleInsertModeOfScreenHandler(ServerPlayerEntity player,
            TankScreenHandler tankScreenHandler) {
        ItemStack tank = tankScreenHandler.getTankItem();
        TankOptions options = Util.getOrCreateOptions(tank);
        options.insertMode = options.insertMode.next();
        Util.setOptions(tank, options);

        // dock.markDirty if has dock pos
        tankScreenHandler.getContext().run(
                (world, blockPos) -> world
                        .getBlockEntity(blockPos, TankStorage.TANK_DOCK_BLOCK_ENTITY)
                        .ifPresent(dock -> {
                            if (dock.hasTank()) {
                                Util.setOptions(dock.getTank(), options);
                                dock.markDirty();
                            }
                        }));
    }

    private static void toggleInsertModeOfHeldTank(ServerPlayerEntity player) {
        ItemStack stack;
        if (Util.isTankLike(player.getMainHandStack()))
            stack = player.getMainHandStack();
        else if (Util.isTankLike(player.getOffHandStack()))
            stack = player.getOffHandStack();
        else
            return;

        TankOptions options = Util.getOrCreateOptions(stack);
        options.insertMode = options.insertMode.next();
        Util.setOptions(stack, options);
        player.sendMessage(Text.translatable("popup.tankstorage.insertmode."
                + options.insertMode.toString().toLowerCase()), true);
    }

}
