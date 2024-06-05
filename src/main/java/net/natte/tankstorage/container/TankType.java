package net.natte.tankstorage.container;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.item.TankItem;
import net.natte.tankstorage.screenhandler.TankScreenHandler;
import net.natte.tankstorage.screenhandler.TankScreenHandlerFactory;
import net.natte.tankstorage.util.Util;

public class TankType {

    private String name;
    private long capacity;
    private int width;
    private int height;
    private Item item;
    private ScreenHandlerType<TankScreenHandler> screenHandlerType;

    public TankType(String name, long capacity, int width, int height) {
        this.name = name;
        this.capacity = capacity;
        this.width = width;
        this.height = height;
    }

    public void register() {

        this.item = new TankItem(new Item.Settings().maxCount(1), this);
        Registry.register(Registries.ITEM, Util.ID(this.name), this.item);
        CauldronBehavior.WATER_CAULDRON_BEHAVIOR.put(this.item, CauldronBehavior.CLEAN_DYEABLE_ITEM);

        this.screenHandlerType = new ExtendedScreenHandlerType<TankScreenHandler>(
                TankScreenHandlerFactory::createClientScreenHandler);
        Registry.register(Registries.SCREEN_HANDLER, Util.ID(name), this.screenHandlerType);

        FluidStorage.ITEM.registerForItems(Util::getFluidStorageFromItemContext, this.item);

    }

    public Item getItem() {
        return this.item;
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

    public long getCapacity() {
        return capacity;
    }

    public ScreenHandlerType<TankScreenHandler> getScreenhandlerType() {
        return screenHandlerType;
    }

}
