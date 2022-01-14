package douplo.skill.bonus;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.context.*;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.server.world.ServerWorld;


public class SimpleValueSkillBonus extends SkillBonus {

    protected static LootContextType CONTEXT_TYPE = new LootContextType.Builder().require(LootContextParameters.THIS_ENTITY).build();
    private final LootNumberProvider numberProvider;

    public SimpleValueSkillBonus(LootNumberProvider value) {
        this.numberProvider = value;
    }

    public double getValue(Entity player) {
        LootContext.Builder builder = new LootContext.Builder((ServerWorld) player.getWorld()).parameter(LootContextParameters.THIS_ENTITY, player);
        return numberProvider.nextFloat(builder.build(CONTEXT_TYPE));
    }

}
