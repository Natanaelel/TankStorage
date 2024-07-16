package net.natte.tankstorage.compat.emi;

import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import net.natte.tankstorage.client.screen.TankScreen;
import net.natte.tankstorage.gui.FluidSlot;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class TankFluidStackProvider implements EmiStackProvider<TankScreen> {
    @Override
    public EmiStackInteraction getStackAt(TankScreen screen, int x, int y) {
        if (!(screen.getSlotUnderMouse() instanceof FluidSlot fluidSlot))
            return EmiStackInteraction.EMPTY;

        if (fluidSlot.getFluid().isEmpty())
            return EmiStackInteraction.EMPTY;

        FluidStack fluidStack = fluidSlot.getFluid().copyWithAmount(fluidSlot.getAmount() == 0 ? FluidType.BUCKET_VOLUME : fluidSlot.getAmount());

        return new EmiStackInteraction(NeoForgeEmiStack.of(fluidStack), null, false);
    }
}
