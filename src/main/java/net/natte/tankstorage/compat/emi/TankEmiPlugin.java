package net.natte.tankstorage.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import net.natte.tankstorage.client.screen.TankScreen;

@EmiEntrypoint
public class TankEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addDragDropHandler(TankScreen.class, new TankDragDropHandler());
        registry.addStackProvider(TankScreen.class, new TankFluidStackProvider());
    }
}
