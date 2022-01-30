package douplo.crafting.bonus;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.explosion.Explosion;

public class ExplosionCraftBonus extends CraftingBonus{

    private final Identifier predicateId;
    private final float power;
    private final boolean fire;

    public ExplosionCraftBonus(Identifier predicateId, float power, boolean fire) {
        super(EXPLOSION);
        this.predicateId = predicateId;
        this.power = power;
        this.fire = fire;
    }

    @Override
    protected ItemStack applyResult(PlayerEntity entity, ItemStack stack, LootContext lootContext) {
        if (entity.getWorld().getServer().getPredicateManager().get(predicateId).test(lootContext)) {
            entity.getWorld().createExplosion(entity, entity.getX(), entity.getY(), entity.getZ(), power, fire, Explosion.DestructionType.DESTROY);
            entity.sendMessage(Text.of("You do not have the requirements to craft this."), true);
            return ItemStack.EMPTY;
        }
        return stack;
    }
}
