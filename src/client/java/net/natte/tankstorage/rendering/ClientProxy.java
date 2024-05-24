package net.natte.tankstorage.rendering;

import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.text.Text;

public class ClientProxy {
    public static List<Text> getFluidTooltip(FluidVariant variant) {
        return FluidVariantRendering.getTooltip(variant);
    }
}
