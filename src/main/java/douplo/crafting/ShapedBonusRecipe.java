package douplo.crafting;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class ShapedBonusRecipe extends ShapedRecipe implements BonusRecipe<CraftingInventory> {

    private final Identifier id;

    private final List<CraftingBonus> bonuses;

    public static class Serializer implements RecipeSerializer<CraftingRecipe> {

        public static final Serializer INSTANCE = new Serializer();
        public static final Identifier ID = new Identifier("rpg_mod", "crafting_shaped_bonus");

        @Override
        public CraftingRecipe read(Identifier id, JsonObject json) {
            String string = JsonHelper.getString(json, "group", "");
            Map<String, Ingredient> map = readSymbols(JsonHelper.getObject(json, "key"));
            String[] strings = removePadding(getPattern(JsonHelper.getArray(json, "pattern")));
            int i = strings[0].length();
            int j = strings.length;
            DefaultedList<Ingredient> defaultedList = createPatternMatrix(strings, map, i, j);
            ItemStack itemStack = BonusRecipe.readResultStack(JsonHelper.getObject(json,"result"));
            ShapedRecipe recipe = new ShapedRecipe(id, string, i, j, defaultedList, itemStack);
            List<CraftingBonus> bonuses = BonusRecipe.readBonuses(json);
            ShapedBonusRecipe bonusRecipe = new ShapedBonusRecipe(id, bonuses, recipe);
            return bonusRecipe;
        }

        @Override
        public CraftingRecipe read(Identifier id, PacketByteBuf buf) {
            ShapedRecipe recipe = ShapedRecipe.Serializer.SHAPED.read(id, buf);
            return recipe;
        }

        @Override
        public void write(PacketByteBuf buf, CraftingRecipe recipe) {

            ShapedRecipe.Serializer.SHAPED.write(buf, ((ShapedBonusRecipe)recipe).extendedRecipe);
        }
    }

    static Map<String, Ingredient> readSymbols(JsonObject json) {
        HashMap<String, Ingredient> map = Maps.newHashMap();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (entry.getKey().length() != 1) {
                throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            }
            if (" ".equals(entry.getKey())) {
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }
            map.put(entry.getKey(), NBTIngredient.fromJson(entry.getValue()));
        }
        map.put(" ", Ingredient.EMPTY);
        return map;
    }

    static DefaultedList<Ingredient> createPatternMatrix(String[] pattern, Map<String, Ingredient> symbols, int width, int height) {
        DefaultedList<Ingredient> defaultedList = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
        HashSet<String> set = Sets.newHashSet(symbols.keySet());
        set.remove(" ");
        for (int i = 0; i < pattern.length; ++i) {
            for (int j = 0; j < pattern[i].length(); ++j) {
                String string = pattern[i].substring(j, j + 1);
                Ingredient ingredient = symbols.get(string);
                if (ingredient == null) {
                    throw new JsonSyntaxException("Pattern references symbol '" + string + "' but it's not defined in the key");
                }
                set.remove(string);
                defaultedList.set(j + width * i, ingredient);
            }
        }
        if (!set.isEmpty()) {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
        }
        return defaultedList;
    }

    static String[] getPattern(JsonArray json) {
        String[] strings = new String[json.size()];
        if (strings.length > 3) {
            throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
        }
        if (strings.length == 0) {
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        }
        for (int i = 0; i < strings.length; ++i) {
            String string = JsonHelper.asString(json.get(i), "pattern[" + i + "]");
            if (string.length() > 3) {
                throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
            }
            if (i > 0 && strings[0].length() != string.length()) {
                throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
            }
            strings[i] = string;
        }
        return strings;
    }

    static String[] removePadding(String ... pattern) {
        int i = Integer.MAX_VALUE;
        int j = 0;
        int k = 0;
        int l = 0;
        for (int m = 0; m < pattern.length; ++m) {
            String string = pattern[m];
            i = Math.min(i, findFirstSymbol(string));
            int n = findLastSymbol(string);
            j = Math.max(j, n);
            if (n < 0) {
                if (k == m) {
                    ++k;
                }
                ++l;
                continue;
            }
            l = 0;
        }
        if (pattern.length == l) {
            return new String[0];
        }
        String[] m = new String[pattern.length - l - k];
        for (int string = 0; string < m.length; ++string) {
            m[string] = pattern[string + k].substring(i, j + 1);
        }
        return m;
    }

    private static int findFirstSymbol(String line) {
        int i;
        for (i = 0; i < line.length() && line.charAt(i) == ' '; ++i) {
        }
        return i;
    }

    private static int findLastSymbol(String pattern) {
        int i;
        for (i = pattern.length() - 1; i >= 0 && pattern.charAt(i) == ' '; --i) {
        }
        return i;
    }

    private ShapedRecipe extendedRecipe;
    private ItemStack outputDisplay;

    public ShapedBonusRecipe(Identifier id, List<CraftingBonus> bonuses, ShapedRecipe recipe) {
        super(id, recipe.getGroup(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getOutput());
        this.id = id;
        this.bonuses = bonuses;
        this.extendedRecipe = recipe;
    }

    @Override
    public RecipeSerializer<?> getSerializer()  {
        return ShapedRecipe.Serializer.SHAPED;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        return extendedRecipe.matches(inventory, world);
    }

    @Override
    public List<CraftingBonus> getBonuses() {
        return this.bonuses;
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory) {
        return extendedRecipe.craft(craftingInventory);
    }

    @Override
    public boolean fits(int width, int height) {
        return extendedRecipe.fits(width, height);
    }

    @Override
    public ItemStack getOutput() {
        if (outputDisplay != null)
            return outputDisplay;
        return extendedRecipe.getOutput();
    }

    public void setOutputDisplay(ItemStack stack) {
        this.outputDisplay = stack;
    }

    @Override
    public Identifier getId() {
        return id;
    }

}
