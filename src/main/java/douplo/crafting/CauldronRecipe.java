package douplo.crafting;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import douplo.RpgMod;
import douplo.crafting.bonus.BonusRecipe;
import douplo.crafting.bonus.CraftingBonus;
import net.minecraft.block.Block;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.List;

public class CauldronRecipe extends ShapelessRecipe implements BonusRecipe<CraftingInventory> {

    private final Block cauldronType;
    private final Block fireType;

    private final List<CraftingBonus> bonuses;
    private ItemStack outputDisplay;

    public CauldronRecipe(Identifier id, ShapelessRecipe recipe, Identifier cauldronType, Identifier fireType, List<CraftingBonus> bonuses) {
        super(id, recipe.getGroup(), recipe.getOutput(), recipe.getIngredients());
        this.cauldronType = Registry.BLOCK.get(cauldronType);
        this.fireType = Registry.BLOCK.get(fireType);
        this.bonuses = bonuses;
    }

    public static class Serializer implements RecipeSerializer<CraftingRecipe> {

        public static final Serializer INSTANCE = new Serializer();
        public static final Identifier ID = new Identifier("rpg_mod", "cauldron_cooking");

        @Override
        public CraftingRecipe read(Identifier id, JsonObject json) {
            ShapelessRecipe recipe = ShapelessBonusRecipe.readRaw(id, json);

            Identifier cauldronType = new Identifier(json.get("cauldron_type").getAsString());
            Identifier fireType = new Identifier(json.get("fire_type").getAsString());

            List<CraftingBonus> bonuses = BonusRecipe.readBonuses(json);

            CauldronRecipe bonusRecipe = new CauldronRecipe(id, recipe, cauldronType, fireType, bonuses);
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
            ShapelessRecipe recipe = ShapedRecipe.Serializer.SHAPELESS.read(id, buf);
            return recipe;
        }

        @Override
        public void write(PacketByteBuf buf, CraftingRecipe recipe) {
            ShapedRecipe.Serializer.SHAPELESS.write(buf, (ShapelessRecipe) recipe);
        }
    }

    public void setOutputDisplay(ItemStack stack) {
        this.outputDisplay = stack;
    }

    public ItemStack getOutput() {
        if (this.outputDisplay != null)
            return this.outputDisplay;
        return super.getOutput();
    }

    public RecipeType getType() {
        return Recipes.CAULDRON_RECIPE;
    }

    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    public boolean matches(CraftingInventory craftingInventory, World world) {
        boolean recipeOk = super.matches(craftingInventory, world);

        return recipeOk;
    }

    public boolean matches(CraftingInventory input, World world, BlockPos blockPosition) {
        RpgMod.LOGGER.info(world.getBlockState(blockPosition));
        return matches(input, world) && world.getBlockState(blockPosition).getBlock() == cauldronType && world.getBlockState(blockPosition.down()).getBlock() == fireType;
    }

    @Override
    public List<CraftingBonus> getBonuses() {
        return bonuses;
    }

}
