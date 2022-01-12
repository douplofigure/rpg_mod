package douplo.crafting.bonus;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import douplo.crafting.bonus.CraftingBonus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;

public interface BonusRecipe<T extends Inventory> {

    public ItemStack craft(T inventory);

    public default ItemStack craft(T craftingInventory, PlayerEntity player) {

        ItemStack stack = craft(craftingInventory);

        for (CraftingBonus bonus : this.getBonuses())
            stack = bonus.apply(player, stack);

        return stack;
    }

    public void setOutputDisplay(ItemStack display);

    public ItemStack getOutput();

    public List<CraftingBonus> getBonuses();

    public static List<CraftingBonus> readBonuses(JsonObject json) {

        List<CraftingBonus> bonuses = new ArrayList<>();
        if (json.has("bonuses")) {
            JsonArray boni = json.getAsJsonArray("bonuses");
            if (boni != null) {
                for (int i = 0; i < boni.size(); ++i) {
                    bonuses.add(CraftingBonus.fromJSON(boni.get(i).getAsJsonObject()));
                }
            }
        }
        return bonuses;

    }

    public static ItemStack readResultStack(JsonObject json) {
        ItemStack stack = ShapedRecipe.outputFromJson(json);

        if (json.has("nbt")) {
            try {
                NbtCompound tag = NbtHelper.fromNbtProviderString(json.get("nbt").getAsString());
                stack.setNbt(tag);
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        }

        if (json.has("name")) {
            Text name = Text.Serializer.fromJson(json.get("name"));
            stack.setCustomName(name);
        }

        return stack;
    }

}
