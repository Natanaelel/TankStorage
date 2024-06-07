package net.natte.tankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.tankstorage.screenhandler.TankScreenHandlerFactory;
import net.natte.tankstorage.util.Util;

public record OpenTankFromKeyBindPacketC2S() implements FabricPacket {

    public static final PacketType<OpenTankFromKeyBindPacketC2S> PACKET_TYPE = PacketType
            .create(Util.ID("opentankfromkeybind"), OpenTankFromKeyBindPacketC2S::read);

    public static OpenTankFromKeyBindPacketC2S read(PacketByteBuf buf) {
        return new OpenTankFromKeyBindPacketC2S();
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    @Override
    public PacketType<OpenTankFromKeyBindPacketC2S> getType() {
        return PACKET_TYPE;
    }

    public static void receive(OpenTankFromKeyBindPacketC2S packet, ServerPlayerEntity player,
            PacketSender responseSender) {
        int slot = findTank(player);

        if (slot == -1)
            return;
        ItemStack tank = player.getInventory().getStack(slot);
        NamedScreenHandlerFactory screenHandlerFactory = new TankScreenHandlerFactory(
                Util.getOrCreateFluidStorage(tank),
                tank,
                slot,
                ScreenHandlerContext.EMPTY);
        player.openHandledScreen(screenHandlerFactory);
    }

    private static int findTank(ServerPlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (Util.isTankLike(stack))
                return i;
        }
        return -1;
    }
}
