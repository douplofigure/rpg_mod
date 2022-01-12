package douplo.skill;

import com.google.gson.JsonObject;
import douplo.RpgMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class CraftingSkill extends Skill{

    private final Identifier itemTag;

    public static Serializer<CraftingSkill> SERIALIZER = new Serializer<CraftingSkill>() {
        @Override
        public CraftingSkill read(JsonObject json, Identifier identifier, Text displayName) {
            Identifier tagId = new Identifier(json.get("tag").getAsString());
            return new CraftingSkill(identifier, displayName, tagId);
        }

        @Override
        public void write(CraftingSkill skill, JsonObject jsonObject) {

            jsonObject.addProperty("tag", skill.itemTag.toString());

        }
    };

    public CraftingSkill(Identifier id, Text displayName, Identifier itemTag) {
        super(id, displayName);
        this.itemTag = itemTag;
    }

    @Override
    public double getRawValue(PlayerEntity player) {
        List<Item> items = player.getWorld().getServer().getTagManager().getTag(Registry.ITEM_KEY, itemTag, (id) -> {
            RpgMod.LOGGER.error(id);
            return null;
        }).values();

        StatHandler stats = ((ServerPlayerEntity) player).getStatHandler();
        double score = 0.0;
        for (Item i : items) {
            score += stats.getStat(Stats.CRAFTED, i);
        }
        return score;
    }
}
