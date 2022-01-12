package douplo.crafting;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class CauldronRecipeType implements RecipeType<CraftingRecipe> {
    public Optional<CraftingRecipe> match(CraftingRecipe recipe, World world, CraftingInventory craftingInventory, BlockPos blockPosition) {
        if (recipe instanceof CauldronRecipe)
            if (((CauldronRecipe)recipe).matches(craftingInventory, world, blockPosition))
                return Optional.of(recipe);
        return Optional.empty();
    }
}
