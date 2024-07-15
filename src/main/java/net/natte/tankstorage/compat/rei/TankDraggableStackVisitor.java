//package net.natte.tankstorage.compat.rei;
//
//import me.shedaniel.math.Rectangle;
//import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
//import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
//import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
//import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
//import net.minecraft.client.gui.screens.Screen;
//import net.minecraft.world.item.ItemStack;
//import net.natte.tankstorage.client.screen.TankScreen;
//import net.natte.tankstorage.gui.FluidSlot;
//import net.natte.tankstorage.packet.server.LockSlotPacketC2S;
//import net.neoforged.neoforge.capabilities.Capabilities;
//import net.neoforged.neoforge.fluids.FluidStack;
//import net.neoforged.neoforge.fluids.FluidUtil;
//import net.neoforged.neoforge.network.PacketDistributor;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.stream.Stream;
//
//public class TankDraggableStackVisitor implements DraggableStackVisitor<TankScreen> {
//
//    @Override
//    public <R extends Screen> boolean isHandingScreen(R screen) {
//        return screen instanceof TankScreen;
//    }
//
//    @Override
//    public DraggedAcceptorResult acceptDraggedStack(DraggingContext<TankScreen> context, DraggableStack dragged) {
//        if (!(context.getScreen().getSlotUnderMouse() instanceof FluidSlot fluidSlot))
//            return DraggedAcceptorResult.PASS;
//
//        FluidStack fluidStack = getFluid(dragged);
//
//        if (fluidStack == null)
//            return DraggedAcceptorResult.PASS;
//
//        TankScreen screen = context.getScreen();
//
//        // optimistically lock slot on client, will be synced later
//        screen.getMenu().lockSlot(fluidSlot.index, fluidStack);
//
//        PacketDistributor.sendToServer(new LockSlotPacketC2S(screen.getMenu().containerId, fluidSlot.index, fluidStack, true));
//
//        return DraggedAcceptorResult.ACCEPTED;
//    }
//
//    @Override
//    public Stream<BoundsProvider> getDraggableAcceptingBounds(DraggingContext<TankScreen> context, DraggableStack dragged) {
//        FluidStack fluidStack = getFluid(dragged);
//        if (fluidStack == null)
//            return Stream.empty();
//
//
//        TankScreen screen = context.getScreen();
//        int left = screen.getGuiLeft();
//        int top = screen.getGuiTop();
//
//        return screen
//                .getMenu()
//                .slots
//                .stream()
//                .filter(slot -> slot instanceof FluidSlot fluidSlot && (fluidSlot.getAmount() == 0 || !fluidSlot.isLocked() && FluidStack.isSameFluidSameComponents(fluidSlot.getFluid(), fluidStack)))
//                .map(slot -> DraggableStackVisitor.BoundsProvider.ofRectangle(new Rectangle(left + slot.x, top + slot.y, 16, 16)));
//    }
//
//    @Nullable
//    private FluidStack getFluid(DraggableStack draggableStack) {
//        if (draggableStack.getStack().getValue() instanceof dev.architectury.fluid.FluidStack archFluidStack)
//            return new FluidStack(archFluidStack.getFluid().builtInRegistryHolder(), 1, archFluidStack.getPatch());
//        if (draggableStack.getStack().getValue() instanceof ItemStack itemStack)
//            return FluidUtil.getFluidContained(itemStack).orElse(null);
//        return null;
//    }
//}
