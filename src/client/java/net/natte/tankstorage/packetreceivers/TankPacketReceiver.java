package net.natte.tankstorage.packetreceivers;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPacketHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayerEntity;
import net.natte.tankstorage.cache.CachedFluidStorageState;
import net.natte.tankstorage.cache.ClientTankCache;
import net.natte.tankstorage.packet.client.TankPacketS2C;

public class TankPacketReceiver implements PlayPacketHandler<TankPacketS2C> {

    // assumes received packet has newer revision
    @Override
    public void receive(TankPacketS2C packet, ClientPlayerEntity player, PacketSender responseSender) {
        ClientTankCache.put(packet.uuid(),
                new CachedFluidStorageState(packet.uuid(), packet.fluids(), packet.revision()));
    }

}
