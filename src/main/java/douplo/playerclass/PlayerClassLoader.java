package douplo.playerclass;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;


public class PlayerClassLoader {

    private static void loadSkillMultiplier(JsonObject object, PlayerClass playerClass) {

        Identifier skillId = new Identifier(object.get("skill").getAsString());
        double value = object.get("value").getAsDouble();

        playerClass.setSkillMultiplier(skillId, value);

    }

    private static void loadAttribute(JsonObject object, PlayerClass playerClass) {
        Identifier attrId = new Identifier(object.get("attribute").getAsString());
        double value = object.get("value").getAsDouble();

        playerClass.setAttribute(attrId, value);
    }

    public static PlayerClass load(Identifier id, InputStream stream) {

        PlayerClass.registerDefaultClass();

        Gson gson = new GsonBuilder().create();
        JsonReader reader = gson.newJsonReader(new InputStreamReader(stream));
        JsonObject object = gson.fromJson(reader, JsonObject.class);

        Text displayName = Text.Serializer.fromJson(object.get("display"));

        PlayerClass playerClass = new PlayerClass(id, displayName);

        JsonArray bonuses = object.getAsJsonArray("skill_multipliers");
        for (int i = 0; i < bonuses.size(); ++i) {
            loadSkillMultiplier((JsonObject) bonuses.get(i), playerClass);
        }

        JsonArray attributes = object.getAsJsonArray("attributes");
        for (int i = 0; i < attributes.size(); ++i) {
            loadAttribute(attributes.get(i).getAsJsonObject(), playerClass);
        }

        return playerClass;

    }

}
