package net.natte.tankstorage.state;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.container.TankType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TankPersistentState extends SavedData {

    private static final String TANK_DATA_KEY = "tank";
    private final Map<UUID, TankFluidStorageState> TANK_MAP;

    public TankPersistentState() {
        TANK_MAP = new HashMap<>();
    }

    public static TankPersistentState createFromNbt(CompoundTag nbtCompound, HolderLookup.Provider registryLookup) {

        TankPersistentState state = new TankPersistentState();

        state.TANK_MAP.clear();

        TankStorage.LOGGER.debug("Loading tanks from nbt");

        Tag tankNbt = nbtCompound.get(TANK_DATA_KEY);
        List<TankFluidStorageState> tanks = TankSerializer.CODEC.parse(registryLookup.createSerializationContext(NbtOps.INSTANCE), tankNbt).getOrThrow();
        for (TankFluidStorageState tank : tanks)
            state.TANK_MAP.put(tank.uuid, tank);


        TankStorage.LOGGER.debug("Loading done");

        return state;
    }

    @Override
    public CompoundTag save(CompoundTag nbtCompound, HolderLookup.Provider registryLookup) {

        TankStorage.LOGGER.debug("Saving tanks to nbt");

        List<TankFluidStorageState> tanks = List.copyOf(TANK_MAP.values());
        Tag tankNbt = TankSerializer.CODEC.encodeStart(registryLookup.createSerializationContext(NbtOps.INSTANCE), tanks).getOrThrow();
        nbtCompound.put(TANK_DATA_KEY, tankNbt);

        TankStorage.LOGGER.debug("Saving done");

        return nbtCompound;
    }

    @Nullable
    public TankFluidStorageState get(UUID uuid) {
        return this.TANK_MAP.get(uuid);
    }

    @Nullable
    public TankFluidStorageState getOrCreate(UUID uuid, TankType type) {
        TankFluidStorageState tank = this.TANK_MAP.get(uuid);
        if (tank == null) {
            tank = TankFluidStorageState.create(type, uuid);
            set(uuid, tank);
        }
        if (tank.type != type) {
            tank = tank.asType(type);
            set(uuid, tank);
        }
        return tank;
    }

    public void set(UUID uuid, TankFluidStorageState tank) {
        this.TANK_MAP.put(uuid, tank);
    }
}
