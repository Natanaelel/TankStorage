package net.natte.tankstorage.item.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.natte.tankstorage.util.FluidSlotData;

import java.util.List;

public record TankTooltipData(List<FluidSlotData> fluids, int selectedSlot) implements TooltipComponent {
}
