package douplo.crafting.bonus;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public static class FutureItemStack {
        private final Identifier itemId;
        private final int count;
        private final NbtCompound tag;
        private final Optional<Text> name;

        public FutureItemStack(Identifier itemId, int count, NbtCompound tag, Optional<Text> name) {
            this.itemId = itemId;
            this.count = count;
            this.tag = tag;
            this.name = name;
        }

        public ItemStack getStack() {
            ItemStack stack = new ItemStack(Registry.ITEM.get(itemId), count);
            stack.setNbt(tag);
            if (name.isPresent())
                stack.setCustomName(name.get());
            return stack;
        }
    }

    public static ItemStack readResultStack(JsonObject json) {
        Identifier itemId = new Identifier(json.get("item").getAsString());

        int count = 1;
        if (json.has("count"))
            count = json.get("count").getAsInt();

        NbtCompound tag = null;
        if (json.has("nbt")) {
            try {
                tag = NbtHelper.fromNbtProviderString(json.get("nbt").getAsString());
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        }

        Optional<Text> name = Optional.empty();

        if (json.has("name")) {
            Text n = Text.Serializer.fromJson(json.get("name"));
            name = Optional.of(n);
        }

        return new FutureItemStack(itemId, count, tag, name).getStack();
    }

}
