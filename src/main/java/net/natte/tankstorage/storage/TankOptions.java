package net.natte.tankstorage.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.Random;

// uniqueId used for stack distinction for hud renderer
public record TankOptions(InsertMode insertMode, TankInteractionMode interactionMode, short uniqueId) {

    private static final Random random = new Random();

    public static final Codec<TankOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BYTE.fieldOf("insertMode").forGetter(t -> (byte) t.insertMode.ordinal()),
            Codec.BYTE.fieldOf("interactionMode").forGetter(t -> (byte) t.interactionMode.ordinal()),
            Codec.SHORT.fieldOf("uniqueId").forGetter(TankOptions::uniqueId)
    ).apply(instance, TankOptions::of));

    public static final StreamCodec<FriendlyByteBuf, TankOptions> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(InsertMode.class),
            TankOptions::insertMode,
            NeoForgeStreamCodecs.enumCodec(TankInteractionMode.class),
            TankOptions::interactionMode,
            ByteBufCodecs.SHORT,
            TankOptions::uniqueId,
            TankOptions::new);

    public static TankOptions create() {
        return new TankOptions(InsertMode.ALL, TankInteractionMode.OPEN_SCREEN, ((short) random.nextInt()));
    }

    private static TankOptions of(byte insertMode, byte interactionMode, short uniqueId) {
        return new TankOptions(InsertMode.values()[insertMode], TankInteractionMode.values()[interactionMode], uniqueId);
    }

    public TankOptions nextInteractionMode() {
        return new TankOptions(insertMode, interactionMode.next(), uniqueId);
    }

    public TankOptions nextInsertMode() {
        return new TankOptions(insertMode.next(), interactionMode, uniqueId);
    }
}
