package douplo.skill;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;

public class SkillLoader {

    public static Skill load(Identifier id, JsonObject jsonObject) {
        SkillType type = SkillType.valueOf(jsonObject.get("type").getAsString());

        Text text;
        if (jsonObject.has("display")) {
            text = Text.Serializer.fromJson(jsonObject.get("display"));
        } else {
            text = null;
        }

        Skill skill = type.getSerializer().read(jsonObject, id, text);

        if (jsonObject.has("multiplier")) {
            double mult = jsonObject.get("multiplier").getAsDouble();
            skill.setGeneralMultiplier(mult);
        }

        return skill;
    }

    public static Skill load(Identifier id, InputStream stream) {

        Gson gson = new GsonBuilder().create();
        JsonReader reader = gson.newJsonReader(new InputStreamReader(stream));
        JsonObject object = gson.fromJson(reader, JsonObject.class);
        return load(id, object);

    }

}
