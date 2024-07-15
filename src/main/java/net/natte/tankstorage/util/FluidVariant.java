//package net.natte.tankstorage.util;
//
//import com.mojang.serialization.Codec;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import net.minecraft.core.Holder;
//import net.minecraft.core.component.DataComponentMap;
//import net.minecraft.core.component.DataComponentPatch;
//import net.minecraft.core.component.PatchedDataComponentMap;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.util.ExtraCodecs;
//import net.minecraft.world.level.material.Fluid;
//import net.minecraft.world.level.material.Fluids;
//import net.neoforged.neoforge.fluids.FluidStack;
//
//import java.util.Objects;
//import java.util.Optional;
//
//@SuppressWarnings("deprecation")
//public class FluidVariant {
//
//    public static final FluidVariant EMPTY = new FluidVariant();
//
//    private static final Codec<Holder<Fluid>> FLUID_CODEC = BuiltInRegistries.FLUID.holderByNameCodec();
//
//
//    private static final Codec<FluidVariant> CODEC = RecordCodecBuilder.create(instance ->
//            instance.group(
//                    FLUID_CODEC.fieldOf("id").forGetter(FluidVariant::getFluidHolder),
//                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidVariant::getComponentPatch)
//            ).apply(instance, FluidVariant::new));
//
//    public static final Codec<FluidVariant> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(
//            optional -> optional.orElse(EMPTY),
//            fluidVariant -> fluidVariant == EMPTY ? Optional.empty() : Optional.of(fluidVariant));
//    Codec<IllegalAccessError> a = BuiltInRegistries.FLUID.holderByNameCodec()
//
//
//    private final Fluid fluid;
//    private final PatchedDataComponentMap components;
//
//    private FluidVariant(Fluid fluid, PatchedDataComponentMap components) {
//        this.fluid = fluid;
//        this.components = components;
//    }
//
//    private FluidVariant() {
//        this.fluid = Fluids.EMPTY;
//        this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
//    }
//
//    public FluidVariant(Holder<Fluid> fluidHolder, DataComponentPatch dataComponentPatch) {
//        this(fluidHolder.value(), PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, dataComponentPatch));
//    }
//
//    public static FluidVariant of(FluidStack fluidStack) {
//        if (fluidStack.isEmpty())
//            return EMPTY;
//        return new FluidVariant(fluidStack.getFluid(), fluidStack.getComponents());
//    }
//
//    public FluidStack toStack(int amount) {
//        if (this == EMPTY)
//            return FluidStack.EMPTY;
//        return new FluidStack(fluid.builtInRegistryHolder(), amount, components.asPatch());
//    }
//
//    private Holder<Fluid> getFluidHolder() {
//        return fluid.builtInRegistryHolder();
//    }
//
//    private DataComponentPatch getComponentPatch() {
//        return components.asPatch();
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o)
//            return true;
//        if (!(o instanceof FluidVariant that))
//            return false;
//        return Objects.equals(fluid, that.fluid) && Objects.equals(components, that.components);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(fluid, components);
//    }
//}
