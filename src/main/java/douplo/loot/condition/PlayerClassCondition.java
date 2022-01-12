package douplo.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import douplo.RpgMod;
import douplo.playerclass.PlayerClass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonSerializer;

public class PlayerClassCondition implements LootCondition {

    public static final JsonSerializer<PlayerClassCondition> SERIALIZER = new JsonSerializer<PlayerClassCondition>() {
        @Override
        public void toJson(JsonObject json, PlayerClassCondition object, JsonSerializationContext context) {
            json.addProperty("class", object.playerClass.toString());
        }

        @Override
        public PlayerClassCondition fromJson(JsonObject json, JsonDeserializationContext context) {

            Identifier classId = new Identifier(json.get("class").getAsString());

            return new PlayerClassCondition(classId);
        }
    };

    private final Identifier playerClass;

    public PlayerClassCondition(Identifier playerClass) {
        this.playerClass = playerClass;
    }

    @Override
    public LootConditionType getType() {
        return LootConditions.PLAYER_CLASS;
    }

    @Override
    public boolean test(LootContext context) {
        PlayerEntity entity = (PlayerEntity) context.get(LootContextParameters.THIS_ENTITY);
        PlayerClass clazz = RpgMod.PLAYER_CLASSES.getPlayerClassFromUUID(entity.getUuid());

        RpgMod.LOGGER.info(clazz.getId());
        RpgMod.LOGGER.info(this.playerClass);

        return clazz.getId().equals(this.playerClass);
    }
}
