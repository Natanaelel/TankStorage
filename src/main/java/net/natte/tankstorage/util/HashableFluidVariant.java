package net.natte.tankstorage.util;
import net.neoforged.neoforge.fluids.FluidStack;

public record HashableFluidVariant(FluidStack fluidStack) {
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof HashableFluidVariant other))
            return false;
        if (fluidStack.isEmpty() && other.fluidStack.isEmpty())
            return true;
        return fluidStack.is(other.fluidStack.getFluid()) && fluidStack.getComponents().equals(other.fluidStack.getComponents());
    }

    @Override
    public int hashCode() {
        return FluidStack.hashFluidAndComponents(fluidStack);
    }
}