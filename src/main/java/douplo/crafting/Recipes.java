package douplo.crafting;

import douplo.RpgMod;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Recipes {

    public static final CauldronRecipeType CAULDRON_RECIPE = new CauldronRecipeType();

    public static void registerRecipes() {

        Registry.register(Registry.RECIPE_SERIALIZER, ShapedBonusRecipe.Serializer.ID, ShapedBonusRecipe.Serializer.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, ShapelessBonusRecipe.ID, ShapelessBonusRecipe.SERIALIZER);
        Registry.register(Registry.RECIPE_SERIALIZER, CauldronRecipe.Serializer.ID, CauldronRecipe.Serializer.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, SmithingBonusRecipe.ID, SmithingBonusRecipe.SERIALIZER);

        Registry.register(Registry.RECIPE_TYPE, new Identifier(RpgMod.MODID, "cauldron_cooking"), CAULDRON_RECIPE);

        if (Registry.RECIPE_SERIALIZER.get(ShapedBonusRecipe.Serializer.ID) == null)
            throw new NullPointerException("Registered recipe missing from registry");

    }

}
