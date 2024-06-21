package net.natte.tankstorage.item;

import net.natte.tankstorage.container.TankType;

public class TankItem extends TankFunctionality {

    public final TankType type;

    public TankItem(Properties settings, TankType type) {
        super(settings);
        this.type = type;
    }
}
