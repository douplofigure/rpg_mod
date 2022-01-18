package douplo.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import douplo.RpgMod;
import douplo.crafting.bonus.BonusRecipe;
import douplo.crafting.bonus.CraftingBonus;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public class ShapelessBonusRecipe extends ShapelessRecipe implements BonusRecipe<CraftingInventory> {

    private final ShapelessRecipe extendedRecipe;
    private final List<CraftingBonus> bonuses;
    private ItemStack outputDisplay;

    public static ShapelessRecipe readRaw(Identifier identifier, JsonObject jsonObject) {
        String string = JsonHelper.getString(jsonObject, "group", "");
        DefaultedList<Ingredient> defaultedList = getIngredients(JsonHelper.getArray(jsonObject, "ingredients"));
        if (defaultedList.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
        } else if (defaultedList.size() > 9) {
            throw new JsonParseException("Too many ingredients for shapeless recipe");
        } else {
            ItemStack itemStack = BonusRecipe.readResultStack(JsonHelper.getObject(jsonObject, "result"));
            return new ShapelessRecipe(identifier, string, itemStack, defaultedList);
        }
    }

    private static DefaultedList<Ingredient> getIngredients(JsonArray json) {
        DefaultedList<Ingredient> defaultedList = DefaultedList.of();

        for(int i = 0; i < json.size(); ++i) {
            Ingredient ingredient = NBTIngredient.fromJson(json.get(i));
            if (!ingredient.isEmpty()) {
                defaultedList.add(ingredient);
            }
        }

        return defaultedList;
    }

    public static final RecipeSerializer<CraftingRecipe> SERIALIZER = new RecipeSerializer<CraftingRecipe>() {

        @Override
        public CraftingRecipe read(Identifier id, JsonObject json) {
            ShapelessRecipe recipe = readRaw(id, json);
            List<CraftingBonus> bonuses = BonusRecipe.readBonuses(json);

            ShapelessBonusRecipe bonusRecipe = new ShapelessBonusRecipe(id, recipe, bonuses);

            if (JsonHelper.getObject(json, "result").has("nbt")) {
                try {
                    NbtCompound tag = NbtHelper.fromNbtProviderString(JsonHelper.getObject(json, "result").get("nbt").getAsString());
                    ItemStack stack = new ItemStack(recipe.getOutput().getItem(), recipe.getOutput().getCount());
                    stack.setNbt(tag);
                    bonusRecipe.setOutputDisplay(stack);
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }

            return bonusRecipe;
        }

        @Override
        public CraftingRecipe read(Identifier id, PacketByteBuf buf) {
            return Serializer.SHAPELESS.read(id, buf);
        }

        @Override
        public void write(PacketByteBuf buf, CraftingRecipe recipe) {
            Serializer.SHAPELESS.write(buf, (ShapelessRecipe) recipe);
        }
    };

    public static final Identifier ID = new Identifier(RpgMod.MODID, "crafting_shapeless_bonus");

    public ShapelessBonusRecipe(Identifier id, ShapelessRecipe recipe, List<CraftingBonus> bonuses) {
        super(id, recipe.getGroup(), recipe.getOutput(), recipe.getIngredients());
        this.extendedRecipe = recipe;
        this.bonuses = bonuses;
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory) {
        return extendedRecipe.craft(craftingInventory);
    }

    @Override
    public void setOutputDisplay(ItemStack display) {
        this.outputDisplay = display;
    }

    @Override
    public ItemStack getOutput() {
        if (this.outputDisplay != null)
            return outputDisplay;
        return super.getOutput();
    }

    @Override
    public List<CraftingBonus> getBonuses() {
        return bonuses;
    }
}
