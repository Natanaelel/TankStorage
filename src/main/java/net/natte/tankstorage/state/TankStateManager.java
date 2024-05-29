package net.natte.tankstorage.state;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.natte.tankstorage.TankStorage;

public class TankStateManager {

    private static TankPersistentState INSTANCE;

    public static TankPersistentState getState() {
        INSTANCE.markDirty();
        return INSTANCE;
    }

    // must be called on server start
    public static void initialize(MinecraftServer server) {
        INSTANCE = getState(server);
    }

    private static TankPersistentState getState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        TankPersistentState state = persistentStateManager.getOrCreate(
                TankPersistentState::createFromNbt,
                TankPersistentState::new,
                TankStorage.MOD_ID);

        return state;
    }
}
