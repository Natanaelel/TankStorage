package net.natte.tankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.screenhandler.TankScreenHandler;
import net.natte.tankstorage.storage.TankOptions;
import net.natte.tankstorage.util.Util;

public record UpdateTankOptionsPacketC2S(TankOptions options) implements FabricPacket {

    public static final PacketType<UpdateTankOptionsPacketC2S> PACKET_TYPE = PacketType.create(Util.ID("options_c2s"),
            UpdateTankOptionsPacketC2S::read);

    public static UpdateTankOptionsPacketC2S read(PacketByteBuf buf) {
        return new UpdateTankOptionsPacketC2S(TankOptions.read(buf));
    }

    @Override
    public void write(PacketByteBuf buf) {
        options.write(buf);
    }

    @Override
    public PacketType<UpdateTankOptionsPacketC2S> getType() {
        return PACKET_TYPE;
    }

    public static void receive(UpdateTankOptionsPacketC2S packet, ServerPlayerEntity playerEntity,
            PacketSender responseServer) {
        if (playerEntity.currentScreenHandler instanceof TankScreenHandler tankScreenHandler) {
            ScreenHandlerContext context = tankScreenHandler.getContext();
            if (context != ScreenHandlerContext.EMPTY) {
                context.run((world, blockPos) -> {
                    world.getBlockEntity(blockPos, TankStorage.TANK_DOCK_BLOCK_ENTITY).ifPresent(dock -> {
                        if (dock.hasTank()) {
                            Util.setOptions(dock.getTank(), packet.options);
                        }
                    });
                });
                return;
            }
        }
        ItemStack stack;
        stack = playerEntity.getMainHandStack();
        if (Util.isTankLike(stack) && Util.hasUUID(stack)) {
            Util.setOptions(stack, packet.options);
            return;
        }
        stack = playerEntity.getOffHandStack();
        if (Util.isTankLike(stack) && Util.hasUUID(stack)) {
            Util.setOptions(stack, packet.options);
            return;
        }
    }

}
