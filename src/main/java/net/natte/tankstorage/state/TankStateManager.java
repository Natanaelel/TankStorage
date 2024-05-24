package net.natte.tankstorage.state;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.natte.tankstorage.TankStorage;

public class TankStateManager {

    public static TankPersistentState getState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        TankPersistentState state = persistentStateManager.getOrCreate(
                TankPersistentState::createFromNbt,
                TankPersistentState::new,
                TankStorage.MOD_ID);

        state.markDirty();

        return state;
    }
}
