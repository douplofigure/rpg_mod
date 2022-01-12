package douplo.skill;

import com.google.gson.JsonObject;
import douplo.RpgMod;
import douplo.playerclass.PlayerClass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class Skill {

    private static Map<Identifier, Skill> namedSkills = new HashMap<>();
    private final Identifier id;
    private final Text displayName;
    private double generalMultiplier;

    public interface Serializer<T extends Skill> {

        T read(JsonObject json, Identifier identifier, Text displayName);
        void write(T skill, JsonObject jsonObject);

    }

    public Skill(Identifier id, Text displayName) {
        this.id = id;
        this.displayName = displayName;
        this.generalMultiplier = 1.0;

        if (id != null) {
            namedSkills.put(id, this);
        }
    }

    public Identifier getId() {
        return id;
    }

    public Text getDisplayName() {
        return displayName;
    }

    public static Skill getFromId(Identifier id) {
        return namedSkills.get(id);
    }

    protected abstract double getRawValue(PlayerEntity player);

    protected double getResult(PlayerEntity player) {
        return this.generalMultiplier * getRawValue(player);
    }

    public double getForPlayer(PlayerEntity player) {

        PlayerClass clazz = RpgMod.PLAYER_CLASSES.getPlayerClassFromUUID(player.getUuid());
        double mult = clazz.getSkillMultiplier(id);

        return mult * getResult(player);

    }

    public static Set<Identifier> listNamedSkills() {
        return namedSkills.keySet();
    }

    public static void clearSkills() {
        namedSkills.clear();
    }

    public void setGeneralMultiplier(double mult) {
        this.generalMultiplier = mult;
    }

}
