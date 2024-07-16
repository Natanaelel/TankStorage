package net.natte.tankstorage.compat.emi;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.material.Fluid;
import net.natte.tankstorage.client.screen.TankScreen;
import net.natte.tankstorage.gui.FluidSlot;
import net.natte.tankstorage.packet.server.LockSlotPacketC2S;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class TankDragDropHandler implements EmiDragDropHandler<TankScreen> {

    @Override
    public boolean dropStack(TankScreen screen, EmiIngredient ingredient, int x, int y) {
        if (!(screen.getSlotUnderMouse() instanceof FluidSlot fluidSlot))
            return false;

        FluidStack fluidStack = getFluid(ingredient);
        if (fluidStack == null)
            return false;

        // optimistically lock slot on client, will be synced later
        screen.getMenu().lockSlot(fluidSlot.index, fluidStack);

        PacketDistributor.sendToServer(new LockSlotPacketC2S(screen.getMenu().containerId, fluidSlot.index, fluidStack, true));

        return true;
    }

    @Override
    public void render(TankScreen screen, EmiIngredient dragged, GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        FluidStack fluidStack = getFluid(dragged);
        if (fluidStack == null)
            return;

        for (Slot slot : screen.getMenu().slots) {
            if (slot instanceof FluidSlot fluidSlot && (fluidSlot.getAmount() == 0 || !fluidSlot.isLocked() && FluidStack.isSameFluidSameComponents(fluidSlot.getFluid(), fluidStack))) {
                int x = screen.getGuiLeft() + slot.x;
                int y = screen.getGuiTop() + slot.y;
                guiGraphics.fill(x, y, x + 16, y + 16, 0x8822BB33);
            }
        }
    }

    @Nullable
    private FluidStack getFluid(EmiIngredient ingredient) {
        EmiStack emiStack = ingredient.getEmiStacks().getFirst();
        if (emiStack.getKey() instanceof Fluid fluid)
            return new FluidStack(fluid.builtInRegistryHolder(), 1, emiStack.getComponentChanges());
        return FluidUtil.getFluidContained(emiStack.getItemStack()).orElse(null);
    }
}
