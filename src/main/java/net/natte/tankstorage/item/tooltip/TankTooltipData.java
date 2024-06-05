package net.natte.tankstorage.item.tooltip;

import java.util.List;

import net.minecraft.client.item.TooltipData;
import net.natte.tankstorage.util.FluidSlotData;

public record TankTooltipData(List<FluidSlotData> fluids, int selectedSlot) implements TooltipData {
}
