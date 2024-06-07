package net.natte.tankstorage.recipe;

import java.util.Optional;

import com.google.gson.JsonObject;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.natte.tankstorage.util.Util;

public class TankLinkRecipe extends ShapedRecipe {

    public TankLinkRecipe(ShapedRecipe recipe) {
        super(recipe.getId(), "tank_link", recipe.getCategory(), recipe.getWidth(), recipe.getHeight(),
                recipe.getIngredients(), recipe.getOutput(null));
    }

    @Override
    public ItemStack craft(RecipeInputInventory recipeInputInventory, DynamicRegistryManager dynamicRegistryManager) {
        Optional<ItemStack> maybeTankItemStack = recipeInputInventory.getInputStacks().stream()
                .filter(Util::isTank).findFirst();

        if (maybeTankItemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack tank = maybeTankItemStack.get();
        if (!Util.hasUUID(tank)) {
            return ItemStack.EMPTY;
        }
        ItemStack result = super.craft(recipeInputInventory, dynamicRegistryManager);
        result.setNbt(tank.getNbt());
        Util.setType(result, Util.getType(tank));
        return result;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(RecipeInputInventory recipeInputInventory) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(recipeInputInventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack stack = recipeInputInventory.getStack(i);
            if (Util.isTank(stack))
                defaultedList.set(i, stack.copyWithCount(1));
        }
        return defaultedList;
    }

    public static class Serializer extends ShapedRecipe.Serializer {

        @Override
        public TankLinkRecipe read(Identifier id, JsonObject json) {
            return new TankLinkRecipe(super.read(id, json));
        }

        @Override
        public TankLinkRecipe read(Identifier id, PacketByteBuf buf) {
            return new TankLinkRecipe(super.read(id, buf));
        }
    }
}
