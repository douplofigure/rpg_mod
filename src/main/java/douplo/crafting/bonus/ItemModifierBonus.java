package douplo.crafting.bonus;

import douplo.crafting.bonus.CraftingBonus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.*;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class ItemModifierBonus extends CraftingBonus {

    private final Identifier modifier;

    public ItemModifierBonus(Identifier modiferId) {
        super(MODIFIER);
        this.modifier = modiferId;
    }

    @Override
    public ItemStack applyResult(PlayerEntity entity, ItemStack stack, LootContext context) {

        LootFunction func = ((ServerWorld) entity.getWorld()).getServer().getItemModifierManager().get(modifier);

        return func.apply(stack, context);
    }

}
