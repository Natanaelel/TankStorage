package net.natte.tankstorage.state;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.container.TankType;

public class TankPersistentState extends PersistentState {

    private static final String TANK_DATA_KEY = "tank_data";
    private final Map<UUID, TankFluidStorageState> TANK_MAP;

    private static TankPersistentState INSTANCE;

    public TankPersistentState() {
        TANK_MAP = new HashMap<>();
    }

    @Nullable
    public static TankPersistentState getInstance() {
        return INSTANCE;
    }

    public static TankPersistentState createFromNbt(NbtCompound nbtCompound) {

        TankPersistentState state = new TankPersistentState();

        state.TANK_MAP.clear();

        TankStorage.LOGGER.debug("Loading tanks from nbt");

        NbtCompound tankNbt = nbtCompound.getCompound(TANK_DATA_KEY);
        TankSerializer.readNbt(state.TANK_MAP, tankNbt);

        TankStorage.LOGGER.debug("Loading done");

        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbtCompound) {

        TankStorage.LOGGER.debug("Saving tanks to nbt");

        NbtCompound tankNbt = TankSerializer.writeNbt(TANK_MAP);
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
