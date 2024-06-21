package net.natte.tankstorage.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.util.FluidSlotData;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class TankSerializer {

    private static final Codec<FluidSlotData> FLUID_SLOT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidStack.OPTIONAL_CODEC.fieldOf("id").forGetter(FluidSlotData::fluidVariant),
            Codec.INT.fieldOf("amount").forGetter(FluidSlotData::amount),
            Codec.BOOL.fieldOf("locked").forGetter(FluidSlotData::isLocked)
    ).apply(instance, FluidSlotData::new));


    private static final Codec<TankFluidStorageState> TANK_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TankType.CODEC.fieldOf("type").forGetter(TankFluidStorageState::getType),
                    UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(TankFluidStorageState::getUuid),
                    FLUID_SLOT_CODEC.listOf().fieldOf("fluids").forGetter(TankFluidStorageState::getFluidSlots)
            ).apply(instance, TankFluidStorageState::new));

    public static final Codec<List<TankFluidStorageState>> CODEC = TANK_CODEC.listOf();
}
