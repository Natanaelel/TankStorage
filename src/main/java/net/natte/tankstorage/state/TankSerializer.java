package net.natte.tankstorage.state;

import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class TankSerializer {

    public static void readNbt(Map<UUID, TankFluidStorageState> tankMap, NbtCompound nbtCompound) {

        NbtList tanks = nbtCompound.getList("tanks", NbtCompound.LIST_TYPE);
        for (NbtElement nbtElement : tanks) {
            TankFluidStorageState tankFluidStorageState = TankFluidStorageState.readNbt((NbtCompound) nbtElement);
            tankMap.put(tankFluidStorageState.uuid, tankFluidStorageState);
        }
    }

    public static NbtCompound writeNbt(Map<UUID, TankFluidStorageState> tankMap) {

        NbtList tanks = new NbtList();
        for (TankFluidStorageState tank : tankMap.values()) {
            NbtCompound nbt = TankFluidStorageState.writeNbt(tank);
            tanks.add(nbt);
        }
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.put("tanks", tanks);

        return nbtCompound;
    }

}
