package net.natte.tankstorage.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

// used in hud and tooltip rendering
public class ClientTankCache {

    private static final Map<UUID, CachedFluidStorageState> CACHE = new HashMap<>();

    private static final Set<UUID> requestQueue = new HashSet<>();
    private static final Map<UUID, Integer> throddledQueue = new HashMap<>();

    public static void put(UUID uuid, CachedFluidStorageState state) {
        CACHE.put(uuid, state);
    }

    @Nullable
    public static CachedFluidStorageState get(UUID uuid) {
        return CACHE.get(uuid);
    }

    @Nullable
    public static CachedFluidStorageState getOrQueueUpdate(UUID uuid) {
        CachedFluidStorageState state = get(uuid);
        if (state == null) {
            requestQueue.add(uuid);
        }
        return state;
    }

    @Nullable
    public static CachedFluidStorageState getAndQueueThrottledUpdate(UUID uuid, int ticks) {
        if (throddledQueue.containsKey(uuid)) {
            return get(uuid);
        } else {
            throddledQueue.put(uuid, ticks);
            requestQueue.add(uuid);
            return get(uuid);
        }
    }

    public static Set<UUID> getQueue() {
        return requestQueue;
    }

    public static void clear() {
        requestQueue.clear();
        CACHE.clear();
    }

    public static void advanceThrottledQueue() {
        throddledQueue.entrySet().removeIf(entity -> entity.getValue() <= 0);
        throddledQueue.replaceAll((uuid, ticksLeft) -> ticksLeft - 1);
    }
}
