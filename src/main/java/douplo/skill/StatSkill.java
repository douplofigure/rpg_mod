package douplo.skill;

import com.google.gson.JsonObject;
import douplo.RpgMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class StatSkill extends Skill {

    public static final Serializer<StatSkill> SERIALIZER = new Serializer<StatSkill>() {
        @Override
        public StatSkill read(JsonObject json, Identifier identifier, Text displayName) {
            Identifier statId = new Identifier(json.get("stat").getAsString());
            return new StatSkill(identifier, displayName, statId);
        }

        @Override
        public void write(StatSkill skill, JsonObject jsonObject) {

        }
    };

    private final Identifier statId;

    public StatSkill(Identifier id, Text displayName, Identifier statId) {
        super(id, displayName);
        this.statId = statId;
    }

    @Override
    protected double getRawValue(PlayerEntity player) {
        StatHandler stats = ((ServerPlayerEntity) player).getStatHandler();
        return stats.getStat(Stats.CUSTOM, statId);
    }
}
