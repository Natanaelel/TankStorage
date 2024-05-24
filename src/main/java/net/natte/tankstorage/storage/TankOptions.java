package net.natte.tankstorage.storage;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

public class TankOptions {
    public InsertMode insertMode;

    public TankOptions() {
        this.insertMode = InsertMode.ALL;
    }

    public static TankOptions fromNbt(NbtCompound nbt) {
        TankOptions options = new TankOptions();
        int b = MathHelper.clamp(nbt.getByte("insertmode"), 0, InsertMode.values().length);
        options.insertMode = InsertMode.values()[b];
        return options;
    }

    public NbtCompound asNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putByte("insertmode", (byte) insertMode.ordinal());
        return nbt;
    }
}
