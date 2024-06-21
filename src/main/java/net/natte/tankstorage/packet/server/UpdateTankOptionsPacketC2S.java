package net.natte.tankstorage.packet.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.screenhandler.TankScreenHandler;
import net.natte.tankstorage.storage.TankOptions;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateTankOptionsPacketC2S(TankOptions options) implements CustomPacketPayload {

    public static final Type<UpdateTankOptionsPacketC2S> TYPE = new Type<>(Util.ID("options_c2s"));
    public static final StreamCodec<FriendlyByteBuf, UpdateTankOptionsPacketC2S> STREAM_CODEC = TankOptions.STREAM_CODEC.map(UpdateTankOptionsPacketC2S::new, UpdateTankOptionsPacketC2S::options);

    @Override
    public Type<UpdateTankOptionsPacketC2S> type() {
        return TYPE;
    }

    public static void receive(UpdateTankOptionsPacketC2S packet, IPayloadContext context){
        Player player = context.player();
        if (player.containerMenu instanceof TankScreenHandler tankScreenHandler) {
            ContainerLevelAccess ccontext = tankScreenHandler.getContext();
            if (ccontext != ContainerLevelAccess.NULL) {
                ccontext.execute((world, blockPos) -> {
                    world.getBlockEntity(blockPos, TankStorage.TANK_DOCK_BLOCK_ENTITY.get()).ifPresent(dock -> {
                        if (dock.hasTank()) {
                            Util.setOptions(dock.getTank(), packet.options);
                        }
                    });
                });
                return;
            }
        }
        ItemStack stack;
        stack = player.getMainHandItem();
        if (Util.isTankLike(stack) && Util.hasUUID(stack)) {
            Util.setOptions(stack, packet.options);
            return;
        }
        stack = player.getOffhandItem();
        if (Util.isTankLike(stack) && Util.hasUUID(stack)) {
            Util.setOptions(stack, packet.options);
            return;
        }
    }
}
