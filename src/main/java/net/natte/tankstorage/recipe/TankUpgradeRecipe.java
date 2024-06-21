package net.natte.tankstorage.recipe;

import java.util.Optional;

import com.google.gson.JsonObject;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.natte.tankstorage.util.Util;

public class TankUpgradeRecipe extends ShapedRecipe {

    public TankUpgradeRecipe(ShapedRecipe recipe) {
        super(recipe.getId(), "tank_upgrade", recipe.getCategory(), recipe.getWidth(), recipe.getHeight(),
                recipe.getIngredients(), recipe.getOutput(null));
    }

    @Override
    public ItemStack craft(RecipeInputInventory recipeInputInventory, DynamicRegistryManager dynamicRegistryManager) {
        Optional<ItemStack> maybeTankItemStack = recipeInputInventory.getInputStacks().stream()
                .filter(Util::isTank).findFirst();

        if (maybeTankItemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = super.craft(recipeInputInventory, dynamicRegistryManager);
        result.setNbt(maybeTankItemStack.get().getNbt());

        return result;
    }

    public static class Serializer extends ShapedRecipe.Serializer {

        @Override
        public TankUpgradeRecipe read(Identifier id, JsonObject json) {
            return new TankUpgradeRecipe(super.read(id, json));
        }

        @Override
        public TankUpgradeRecipe read(Identifier id, PacketByteBuf buf) {
            return new TankUpgradeRecipe(super.read(id, buf));

        }
    }
}
