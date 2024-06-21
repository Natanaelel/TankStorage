package net.natte.tankstorage.state;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.natte.tankstorage.TankStorage;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public class TankStateManager {

    public static final SavedData.Factory<TankPersistentState> TYPE = new SavedData.Factory<>(
            TankPersistentState::new,
            TankPersistentState::createFromNbt,
            null);

    private static TankPersistentState INSTANCE;

    public static TankPersistentState getState() {
        INSTANCE.setDirty();
        return INSTANCE;
    }

    // must be called on server start
    public static void initialize(ServerStartedEvent event) {
        INSTANCE = getState(event.getServer());
    }

    private static TankPersistentState getState(MinecraftServer server) {
        DimensionDataStorage persistentStateManager = server.overworld().getDataStorage();

        return persistentStateManager.computeIfAbsent(TYPE, TankStorage.MOD_ID);
    }
}
