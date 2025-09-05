package net.natte.tankstorage.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.util.Util;

import java.util.Optional;

public class TankLinkRecipe extends ShapedRecipe {

    public TankLinkRecipe(ShapedRecipe recipe) {
        super(recipe.getGroup(), recipe.category(), recipe.pattern, recipe.result);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (!super.matches(input, level))
            return false;

        return input
                .items()
                .stream()
                .filter(Util::isTankLike)
                .findFirst()
                .map(Util::hasUUID)
                .orElse(false);
    }

    @Override
    public ItemStack assemble(CraftingInput recipeInputInventory, HolderLookup.Provider registryLookup) {
        Optional<ItemStack> maybeTankItemStack = recipeInputInventory.items().stream()
                .filter(Util::isTankLike).findFirst();

        if (maybeTankItemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack tank = maybeTankItemStack.get();
        if (!Util.hasUUID(tank)) {
            return ItemStack.EMPTY;
        }
        ItemStack result = super.assemble(recipeInputInventory, registryLookup);
        result.applyComponents(tank.getComponentsPatch());
        result.set(TankStorage.TankTypeComponentType, Util.getType(tank));
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput recipeInputInventory) {
        NonNullList<ItemStack> remainingItems = super.getRemainingItems(recipeInputInventory);
        for (int i = 0; i < remainingItems.size(); ++i) {
            ItemStack stack = recipeInputInventory.getItem(i);
            if (Util.isTankLike(stack))
                remainingItems.set(i, stack.copyWithCount(1));
        }
        return remainingItems;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TankStorage.TANK_LINK_RECIPE.get();
    }

    public static class Serializer implements RecipeSerializer<TankLinkRecipe> {
        public static final MapCodec<TankLinkRecipe> CODEC = ShapedRecipe.Serializer.CODEC.xmap(TankLinkRecipe::new, ShapedRecipe.class::cast);
        public static final StreamCodec<RegistryFriendlyByteBuf, TankLinkRecipe> STREAM_CODEC = ShapedRecipe.Serializer.STREAM_CODEC.map(TankLinkRecipe::new, ShapedRecipe.class::cast);

        @Override
        public MapCodec<TankLinkRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TankLinkRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}