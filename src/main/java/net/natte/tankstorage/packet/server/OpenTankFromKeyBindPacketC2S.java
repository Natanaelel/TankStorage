package net.natte.tankstorage.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.natte.tankstorage.screenhandler.TankMenuFactory;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenTankFromKeyBindPacketC2S() implements CustomPacketPayload {


    public static final OpenTankFromKeyBindPacketC2S INSTANCE = new OpenTankFromKeyBindPacketC2S();
    public static final Type<OpenTankFromKeyBindPacketC2S> TYPE = new Type<>(Util.ID("opentankfromkeybind"));
    public static final StreamCodec<ByteBuf, OpenTankFromKeyBindPacketC2S> STREAM_CODEC = StreamCodec.unit(INSTANCE);


    @Override
    public Type<OpenTankFromKeyBindPacketC2S> type() {
        return TYPE;
    }

    public static void receive(OpenTankFromKeyBindPacketC2S packet, IPayloadContext context) {

        ServerPlayer player = (ServerPlayer) context.player();
        int slot = findTank(player);

        if (slot == -1)
            return;
        ItemStack tank = player.getInventory().getItem(slot);
        TankMenuFactory menu = new TankMenuFactory(
                Util.getOrCreateFluidStorage(tank),
                tank,
                slot,
                ContainerLevelAccess.NULL);
        menu.open(player);
    }

    private static int findTank(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (Util.isTankLike(stack) && Util.getOrCreateFluidStorage(stack) != null)
                return i;
        }
        return -1;
    }
}
