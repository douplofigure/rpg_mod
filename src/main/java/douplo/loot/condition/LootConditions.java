package douplo.loot.condition;

import douplo.RpgMod;
import douplo.playerclass.PlayerClass;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;


public class LootConditions {

    public static final LootConditionType PLAYER_CLASS = new LootConditionType(PlayerClassCondition.SERIALIZER);

    public static void register() {
        Registry.register(Registry.LOOT_CONDITION_TYPE, new Identifier(RpgMod.MODID, "player_class"), PLAYER_CLASS);
    }

}
