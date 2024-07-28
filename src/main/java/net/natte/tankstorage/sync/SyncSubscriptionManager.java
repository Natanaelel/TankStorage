package net.natte.tankstorage.sync;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

public class SyncSubscriptionManager {
    private static final Map<UUID, Set<UUID>> subscriptions = new HashMap<>();
    private static final Set<UUID> uuidsToSync = new HashSet<>();

    private static MinecraftServer server;

    public static void tick(ServerTickEvent.Post event) {
        server = event.getServer();

        uuidsToSync.forEach(SyncSubscriptionManager::sync);
        uuidsToSync.clear();
    }

    public static void setChanged(UUID tankUuid) {
        uuidsToSync.add(tankUuid);
    }

    public static void subscribe(UUID playerUuid, UUID tankUuid) {
        subscriptions
                .computeIfAbsent(tankUuid, k -> new HashSet<>())
                .add(playerUuid);
    }

    public static void unsubscribe(UUID playerUuid, UUID tankUuid) {
        Set<UUID> tankSubscriptions = subscriptions.get(tankUuid);
        if (tankSubscriptions != null)
            tankSubscriptions.remove(playerUuid);
    }

    private static void sync(UUID tankUuid) {
        if (server == null)
            return;

        TankFluidStorageState tank = Util.getFluidStorage(tankUuid);
        if (tank == null)
            return;

        Set<UUID> tankSubscriptions = subscriptions.get(tankUuid);
        if (tankSubscriptions == null)
            return;

        tankSubscriptions.forEach(playerUuid -> {
            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
            if (player == null)
                return;
            tank.sync(player);
        });
    }
}
