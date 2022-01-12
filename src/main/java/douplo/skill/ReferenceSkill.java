package douplo.skill;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ReferenceSkill extends Skill{

    public static final Serializer<ReferenceSkill> SERIALIZER = new Serializer<ReferenceSkill>() {
        @Override
        public ReferenceSkill read(JsonObject json, Identifier identifier, Text displayName) {
            Identifier refId = new Identifier(json.get("skill").getAsString());
            return new ReferenceSkill(identifier, displayName, refId);
        }

        @Override
        public void write(ReferenceSkill skill, JsonObject jsonObject) {

        }
    };

    private final Identifier refId;

    public ReferenceSkill(Identifier id, Text displayName, Identifier refId) {
        super(id, displayName);
        this.refId = refId;
    }

    @Override
    protected double getRawValue(PlayerEntity player) {
        return Skill.getFromId(refId).getResult(player);
    }
}
