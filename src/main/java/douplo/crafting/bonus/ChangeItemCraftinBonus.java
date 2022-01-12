package douplo.crafting.bonus;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;

public class ChangeItemCraftinBonus extends CraftingBonus {

    private final Item destItem;

    public ChangeItemCraftinBonus(Item destItem) {
        super(CHANGE_ITEM);
        this.destItem = destItem;
    }

    @Override
    public ItemStack applyResult(PlayerEntity entity, ItemStack stack, LootContext context) {
        ItemStack res = new ItemStack(destItem, stack.getCount());
        res.setNbt(stack.getNbt());
        return res;
    }
}
