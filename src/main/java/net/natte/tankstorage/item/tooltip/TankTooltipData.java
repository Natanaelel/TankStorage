package net.natte.tankstorage.item.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.natte.tankstorage.util.LargeFluidSlotData;

import java.util.List;

public record TankTooltipData(List<LargeFluidSlotData> fluids, int selectedSlot) implements TooltipComponent {
}
