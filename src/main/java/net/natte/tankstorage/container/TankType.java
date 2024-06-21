package net.natte.tankstorage.container;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.item.TankItem;
import net.neoforged.neoforge.registries.DeferredHolder;

public class TankType {

    public static final Codec<TankType> CODEC = Codec.STRING.xmap(TankType::fromName, t -> t.name);
    public static final StreamCodec<ByteBuf, TankType> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(TankType::fromName, t -> t.name);

    private String name;
    private int capacity;
    private int width;
    private int height;
    private DeferredHolder<Item, TankItem> item;

    public TankType(String name, int capacity, int width, int height) {
        this.name = name;
        this.capacity = capacity;
        this.width = width;
        this.height = height;
    }

    public void register() {
        this.item = TankStorage.ITEMS.register(this.name, () -> new TankItem(new Item.Properties().stacksTo(1), this));
    }

    public Item getItem() {
        return this.item.get();
    }

    public int size() {
        return width * height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public String getName() {
        return name;
    }

    public static TankType fromName(String name) {
        for (TankType type : TankStorage.TANK_TYPES) {
            if (type.name.equals(name))
                return type;
        }
        return null;
    }

    public int getCapacity() {
        return capacity;
    }
}
