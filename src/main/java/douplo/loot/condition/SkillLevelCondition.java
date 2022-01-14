package douplo.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import douplo.skill.Skill;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonSerializer;

public class SkillLevelCondition implements LootCondition {

    public static final JsonSerializer<SkillLevelCondition> SERIALIZER = new JsonSerializer<SkillLevelCondition>() {
        @Override
        public void toJson(JsonObject json, SkillLevelCondition object, JsonSerializationContext context) {
            json.addProperty("min_value", object.minValue);
            json.addProperty("skill", object.skillId.toString());
        }

        @Override
        public SkillLevelCondition fromJson(JsonObject json, JsonDeserializationContext context) {
            Identifier skillId = new Identifier(json.get("skill").getAsString());
            double minValue = json.get("min_value").getAsDouble();

            return new SkillLevelCondition(minValue, skillId);
        }
    };

    private final double minValue;
    private final Identifier skillId;

    public SkillLevelCondition(double minValue, Identifier skillId) {
        this.minValue = minValue;
        this.skillId = skillId;
    }

    @Override
    public LootConditionType getType() {
        return LootConditions.SKILL_LEVEL;
    }

    @Override
    public boolean test(LootContext context) {
        PlayerEntity entity = (PlayerEntity) context.get(LootContextParameters.THIS_ENTITY);
        return Skill.getFromId(skillId).getForPlayer(entity) >= minValue;
    }
}
