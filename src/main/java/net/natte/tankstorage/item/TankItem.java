package net.natte.tankstorage.item;

import net.minecraft.item.DyeableItem;
import net.natte.tankstorage.container.TankType;

public class TankItem extends TankFunctionality implements DyeableItem {

    public final TankType type;

    public TankItem(Settings settings, TankType type) {
        super(settings);
        this.type = type;
    }

}