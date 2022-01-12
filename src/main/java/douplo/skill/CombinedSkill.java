package douplo.skill;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import douplo.RpgMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CombinedSkill extends Skill{

    private final Skill[] children;
    private final double[] factors;

    public static Serializer<CombinedSkill> SERIALIZER = new Serializer<CombinedSkill>() {
        @Override
        public CombinedSkill read(JsonObject json, Identifier identifier, Text displayName) {

            JsonArray cNodes = json.getAsJsonArray("children");
            Skill[] children = new Skill[cNodes.size()];
            double[] factors;
            if (json.has("factors")) {
                JsonArray arr = json.getAsJsonArray("factors");
                factors = new double[arr.size()];
                for (int i = 0; i < factors.length; ++i) {
                    factors[i] = arr.get(i).getAsDouble();
                }
            } else {
                factors = new double[children.length];
                for (int i = 0; i < factors.length; ++i) {
                    factors[i] = 1.0;
                }
            }

            if (factors.length != children.length)
                return null;

            for (int i = 0; i < cNodes.size(); ++i) {
                children[i] = SkillLoader.load(null, cNodes.get(i).getAsJsonObject());
            }

            return new CombinedSkill(identifier, displayName, children, factors);
        }

        @Override
        public void write(CombinedSkill skill, JsonObject jsonObject) {

        }
    };

    public CombinedSkill(Identifier id, Text displayName, Skill[] children, double[] factors) {
        super(id, displayName);
        this.children = children;
        this.factors = factors;
    }

    @Override
    protected double getRawValue(PlayerEntity player) {
        double v = 0.0;
        for (int i = 0; i < children.length; ++i) {
            double x = children[i].getResult(player);
            v += factors[i] * x;
        }
        return v;
    }
}
