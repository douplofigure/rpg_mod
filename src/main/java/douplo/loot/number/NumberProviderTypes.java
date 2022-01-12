package douplo.loot.number;

import douplo.RpgMod;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class NumberProviderTypes {

    public static final LootNumberProviderType SKILL = new LootNumberProviderType(SkillLootNumberProvider.SERIALIZER);
    public static final LootNumberProviderType OPERATION = new LootNumberProviderType(OperationNumberProvider.SERIALIZER);

    public static void register() {
        Registry.register(Registry.LOOT_NUMBER_PROVIDER_TYPE, new Identifier(RpgMod.MODID, "skill"), SKILL);
        Registry.register(Registry.LOOT_NUMBER_PROVIDER_TYPE, new Identifier(RpgMod.MODID, "operation"), OPERATION);
    }
}
