package douplo.crafting;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import douplo.RpgMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class NBTIngredient extends Ingredient {

    public static class TagEntry
            implements Entry {
        private final Tag<Item> tag;

        public TagEntry(Tag<Item> tag) {
            this.tag = tag;
        }

        @Override
        public Collection<ItemStack> getStacks() {
            ArrayList<ItemStack> list = Lists.newArrayList();
            for (Item item : this.tag.values()) {
                list.add(new ItemStack(item));
            }
            return list;
        }

        @Override
        public JsonObject toJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("tag", ServerTagManagerHolder.getTagManager().getTagId(Registry.ITEM_KEY, this.tag, () -> new IllegalStateException("Unknown item tag")).toString());
            return jsonObject;
        }
    }

    public static class StackEntry
            implements Entry {
        private final ItemStack stack;

        public StackEntry(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public Collection<ItemStack> getStacks() {
            return Collections.singleton(this.stack);
        }

        @Override
        public JsonObject toJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("item", Registry.ITEM.getId(this.stack.getItem()).toString());
            return jsonObject;
        }
    }

    public static class NBTEntry implements Ingredient.Entry {

        private final ItemStack stack;

        public NBTEntry(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public Collection<ItemStack> getStacks() {
            return Collections.singleton(stack);
        }

        @Override
        public JsonObject toJson() {
            JsonObject object = new JsonObject();
            object.addProperty("item", Registry.ITEM.getId(this.stack.getItem()).toString());
            object.addProperty("nbt", stack.getNbt().toString());
            return object;
        }
    }

    private final ItemStack stack;

    public NBTIngredient(ItemStack stack) {
        super(Stream.of(new NBTEntry(stack)));
        RpgMod.LOGGER.info("Creating NBTIngredient");
        this.stack = stack;
    }

    @Override
    public boolean test(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        if (this.stack.isOf(itemStack.getItem())) {
            NbtCompound required = this.stack.getNbt();
            if (required == null) return true;
            return NbtHelper.matches(required, itemStack.getNbt(), false);
        }
        return false;
    }

    private static Ingredient.Entry entryFromJson(JsonObject json) {
        if (json.has("item") && json.has("tag")) {
            throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
        }
        if (json.has("nbt")) {
            Item item = ShapedRecipe.getItem(json);
            ItemStack stack = new ItemStack(item, 1);
            try {
                NbtCompound compound = StringNbtReader.parse(json.get("nbt").getAsString());
                stack.setNbt(compound);
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return new NBTIngredient.NBTEntry(stack);
        }
        if (json.has("item")) {
            Item item = ShapedRecipe.getItem(json);
            return new NBTIngredient.StackEntry(new ItemStack(item));
        }
        if (json.has("tag")) {
            Identifier item = new Identifier(JsonHelper.getString(json, "tag"));
            Tag<Item> tag = ServerTagManagerHolder.getTagManager().getTag(Registry.ITEM_KEY, item, identifier -> new JsonSyntaxException("Unknown item tag '" + identifier + "'"));
            return new NBTIngredient.TagEntry(tag);
        }
        throw new JsonParseException("An ingredient entry needs either a tag or an item");
    }

    private static Ingredient ofEntries(Stream<? extends Ingredient.Entry> entries) {
        Ingredient ingredient = new Ingredient(entries);
        return ingredient;
    }

    public static Ingredient fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Item cannot be null");
        }
        if (json.isJsonObject()) {
            Ingredient.Entry entry = entryFromJson(json.getAsJsonObject());
            if (entry instanceof NBTIngredient.NBTEntry)
                return new NBTIngredient((ItemStack) entry.getStacks().toArray()[0]);
            return ofEntries(Stream.of(entry));
        }
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            if (jsonArray.size() == 0) {
                throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");
            }
            return ofEntries(StreamSupport.stream(jsonArray.spliterator(), false).map(jsonElement -> entryFromJson(JsonHelper.asObject(jsonElement, "item"))));
        }
        throw new JsonSyntaxException("Expected item to be object or array of objects");
    }

}
