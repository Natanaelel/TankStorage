package net.natte.tankstorage.storage;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.MathHelper;

public class TankOptions {

    public InsertMode insertMode;
    public int selectedSlot;
    public TankInteractionMode interactionMode;

    public TankOptions() {
        this.insertMode = InsertMode.ALL;
        this.selectedSlot = -1;
        this.interactionMode = TankInteractionMode.OPEN_SCREEN;
    }

    public static TankOptions fromNbt(NbtCompound nbt) {
        TankOptions options = new TankOptions();
        int b = MathHelper.clamp(nbt.getByte("insertmode"), 0, InsertMode.values().length);
        options.insertMode = InsertMode.values()[b];
        options.selectedSlot = nbt.getInt("selectedslot");
        options.interactionMode = nbt.getBoolean("interactionmode") ? TankInteractionMode.OPEN_SCREEN
                : TankInteractionMode.BUCKET;
        return options;
    }

    public NbtCompound asNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putByte("insertmode", (byte) insertMode.ordinal());
        nbt.putInt("selectedslot", selectedSlot);
        nbt.putBoolean("interactionmode", interactionMode == TankInteractionMode.OPEN_SCREEN);
        return nbt;
    }

    public static TankOptions read(PacketByteBuf buf) {
        TankOptions options = new TankOptions();
        options.insertMode = InsertMode.values()[MathHelper.clamp(buf.readByte(), 0, InsertMode.values().length)];
        options.selectedSlot = buf.readInt();
        options.interactionMode = buf.readBoolean() ? TankInteractionMode.OPEN_SCREEN : TankInteractionMode.BUCKET;
        return options;
    }

    public void write(PacketByteBuf buf) {
        buf.writeByte(insertMode.ordinal());
        buf.writeInt(selectedSlot);
        buf.writeBoolean(interactionMode == TankInteractionMode.OPEN_SCREEN);
    }
}
