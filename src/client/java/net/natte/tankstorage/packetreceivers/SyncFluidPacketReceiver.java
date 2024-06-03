package net.natte.tankstorage.packetreceivers;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPacketHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.natte.tankstorage.packet.screenHandler.SyncFluidPacketS2C;
import net.natte.tankstorage.screenhandler.TankScreenHandler;

public class SyncFluidPacketReceiver implements PlayPacketHandler<SyncFluidPacketS2C> {

    @Override
    public void receive(SyncFluidPacketS2C packet, ClientPlayerEntity player, PacketSender responseSender) {
        System.out.println("update slot start " + packet.slot());

        ScreenHandler screenHandler = player.currentScreenHandler;
        if (packet.syncId() != screenHandler.syncId)
            return;
        if (!(screenHandler instanceof TankScreenHandler tankScreenHandler))
            return;
        System.out.println("update slot " + packet.slot());
        tankScreenHandler.updateFluidSlot(packet.slot(), packet.fluidSlotData());
    }

}
