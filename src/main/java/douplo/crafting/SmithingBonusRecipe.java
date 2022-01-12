package douplo.crafting;

import com.google.gson.JsonObject;
import douplo.RpgMod;
import douplo.crafting.bonus.BonusRecipe;
import douplo.crafting.bonus.CraftingBonus;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.List;

public class SmithingBonusRecipe extends SmithingRecipe implements BonusRecipe<Inventory> {

    public static final Identifier ID = new Identifier(RpgMod.MODID, "smithing_bonus");
    public static final RecipeSerializer<SmithingRecipe> SERIALIZER = new RecipeSerializer<SmithingRecipe>() {
        @Override
        public SmithingRecipe read(Identifier id, JsonObject json) {
            Ingredient ingredient = NBTIngredient.fromJson(JsonHelper.getObject(json, "base"));
            Ingredient ingredient2 = NBTIngredient.fromJson(JsonHelper.getObject(json, "addition"));
            ItemStack itemStack = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));

            List<CraftingBonus> bonuses = BonusRecipe.readBonuses(json);

            return new SmithingBonusRecipe(id, ingredient, ingredient2, itemStack, bonuses);
        }

        @Override
        public SmithingRecipe read(Identifier id, PacketByteBuf buf) {
            return Serializer.SMITHING.read(id, buf);
        }

        @Override
        public void write(PacketByteBuf buf, SmithingRecipe recipe) {

            Serializer.SMITHING.write(buf, recipe);

        }
    };

    private final List<CraftingBonus> bonuses;

    public SmithingBonusRecipe(Identifier id, Ingredient base, Ingredient addition, ItemStack result, List<CraftingBonus> bonuses) {
        super(id, base, addition, result);
        this.bonuses = bonuses;
    }

    @Override
    public void setOutputDisplay(ItemStack display) {

    }

    @Override
    public List<CraftingBonus> getBonuses() {
        return bonuses;
    }
}
