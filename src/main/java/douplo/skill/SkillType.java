package douplo.skill;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SkillType<T extends Skill> {

    private final Identifier id;
    private final Skill.Serializer<T> serializer;

    private static Map<Identifier, SkillType> identifierSkillTypeMap = new HashMap<>();

    public SkillType(Identifier id, Skill.Serializer<T> serializer) {
        this.id = id;
        this.serializer = serializer;
        identifierSkillTypeMap.put(id, this);
    }

    public static SkillType valueOf(String string) {
        return valueOf(new Identifier(string));
    }

    private static SkillType valueOf(Identifier identifier) {
        return identifierSkillTypeMap.get(identifier);
    }

    public Skill.Serializer<T> getSerializer() {
        return serializer;
    }
}
