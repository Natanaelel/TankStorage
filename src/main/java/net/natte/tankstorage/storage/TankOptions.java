package net.natte.tankstorage.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record TankOptions(InsertMode insertMode, TankInteractionMode interactionMode) {

    public static final TankOptions DEFAULT = new TankOptions(InsertMode.ALL, TankInteractionMode.OPEN_SCREEN);

    public static final Codec<TankOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BYTE.fieldOf("insertMode").forGetter(t -> (byte) t.insertMode.ordinal()),
            Codec.BYTE.fieldOf("interactionMode").forGetter(t -> (byte) t.interactionMode.ordinal())
    ).apply(instance, TankOptions::of));


    public static final StreamCodec<FriendlyByteBuf, TankOptions> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(InsertMode.class),
            TankOptions::insertMode,
            NeoForgeStreamCodecs.enumCodec(TankInteractionMode.class),
            TankOptions::interactionMode,
            TankOptions::new);

    private static TankOptions of(byte insertMode, byte interactionMode) {
        return new TankOptions(InsertMode.values()[insertMode], TankInteractionMode.values()[interactionMode]);
    }
}
