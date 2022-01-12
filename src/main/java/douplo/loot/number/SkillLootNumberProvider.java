package douplo.loot.number;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import douplo.loot.LootArguments;
import douplo.skill.Skill;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonSerializer;

import java.util.Set;

public class SkillLootNumberProvider implements LootNumberProvider {

    public static JsonSerializer<LootNumberProvider> SERIALIZER = new JsonSerializer<LootNumberProvider>() {
        @Override
        public void toJson(JsonObject json, LootNumberProvider object, JsonSerializationContext context) {

        }

        @Override
        public LootNumberProvider fromJson(JsonObject json, JsonDeserializationContext context) {
            Identifier skillName = new Identifier(json.get("skill").getAsString());
            return new SkillLootNumberProvider(skillName);
        }
    };

    private final Identifier skillName;

    public SkillLootNumberProvider(Identifier skillName) {
        this.skillName = skillName;
    }

    @Override
    public float nextFloat(LootContext context) {
        PlayerEntity player = (PlayerEntity) context.get(LootContextParameters.THIS_ENTITY);
        return (float) Skill.getFromId(skillName).getForPlayer(player);
    }

    @Override
    public int nextInt(LootContext context) {
        PlayerEntity player = (PlayerEntity) context.get(LootContextParameters.THIS_ENTITY);
        return (int) Skill.getFromId(skillName).getForPlayer(player);
    }

    @Override
    public LootNumberProviderType getType() {
        return NumberProviderTypes.SKILL;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(LootContextParameters.THIS_ENTITY);
    }

    @Override
    public void validate(LootTableReporter reporter) {
        LootNumberProvider.super.validate(reporter);
    }
}
