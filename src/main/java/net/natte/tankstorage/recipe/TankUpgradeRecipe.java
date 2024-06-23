package net.natte.tankstorage.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.util.Util;

import java.util.Optional;

public class TankUpgradeRecipe extends ShapedRecipe {

    public TankUpgradeRecipe(ShapedRecipe recipe) {
        super(recipe.getGroup(), recipe.category(), recipe.pattern, recipe.result);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }


    @Override
    public ItemStack assemble(CraftingInput recipeInputInventory, HolderLookup.Provider registryLookup) {
        Optional<ItemStack> maybeBankItemStack = recipeInputInventory.items().stream()
                .filter(Util::isTank).findFirst();

        if (maybeBankItemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = super.assemble(recipeInputInventory, registryLookup);
        result.applyComponentsAndValidate(maybeBankItemStack.get().getComponentsPatch());

        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TankStorage.TANK_UPGRADE_RECIPE.get();
    }

    public static class Serializer implements RecipeSerializer<TankUpgradeRecipe> {
        public static final MapCodec<TankUpgradeRecipe> CODEC = ShapedRecipe.Serializer.CODEC.xmap(TankUpgradeRecipe::new, ShapedRecipe.class::cast);
        public static final StreamCodec<RegistryFriendlyByteBuf, TankUpgradeRecipe> STREAM_CODEC = ShapedRecipe.Serializer.STREAM_CODEC.map(TankUpgradeRecipe::new, ShapedRecipe.class::cast);

        @Override
        public MapCodec<TankUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TankUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}