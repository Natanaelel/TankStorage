package net.natte.tankstorage.compat.rei;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.screens.Screen;
import net.natte.tankstorage.client.screen.TankScreen;
import net.natte.tankstorage.gui.FluidSlot;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class TankFocusedStackProvider implements FocusedStackProvider {
    @Override
    public CompoundEventResult<EntryStack<?>> provide(Screen screen, Point mouse) {
        if (!(screen instanceof TankScreen tankScreen))
            return CompoundEventResult.pass();

        if (!(tankScreen.getSlotUnderMouse() instanceof FluidSlot fluidSlot))
            return CompoundEventResult.pass();

        if (fluidSlot.getFluid().isEmpty())
            return CompoundEventResult.pass();

        FluidStack fluidStack = fluidSlot.getFluid().copyWithAmount(fluidSlot.getAmount() == 0 ? FluidType.BUCKET_VOLUME : fluidSlot.getAmount());

        return CompoundEventResult.interruptTrue(EntryStacks.of(FluidStackHooksForge.fromForge(fluidStack)));
    }
}
