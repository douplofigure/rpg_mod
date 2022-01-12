package douplo.skill;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.system.CallbackI;

import java.util.List;

public class UseSkill extends Skill{

    private final boolean isTag;
    private final Identifier itemId;

    public static final Serializer<UseSkill> SERIALIZER = new Serializer<UseSkill>() {
        @Override
        public UseSkill read(JsonObject json, Identifier identifier, Text displayName) {
            if (json.has("tag")) {
                Identifier tagId = new Identifier(json.get("tag").getAsString());
                return new UseSkill(identifier, displayName, true, tagId);
            } else {
                Identifier itemId = new Identifier(json.get("item").getAsString());
                return new UseSkill(identifier, displayName, false, itemId);
            }
        }

        @Override
        public void write(UseSkill skill, JsonObject jsonObject) {

        }
    };

    public UseSkill(Identifier id, Text displayName, boolean isTag, Identifier itemId) {
        super(id, displayName);
        this.isTag = isTag;
        this.itemId = itemId;
    }

    @Override
    protected double getRawValue(PlayerEntity player) {
        StatHandler handler = ((ServerPlayerEntity)player).getStatHandler();
        if (!this.isTag)
            return handler.getStat(Stats.USED, Registry.ITEM.get(itemId));
        else {
            double sum = 0.0;
            List<Item> blocks = player.getWorld().getServer().getTagManager().getTag(Registry.ITEM_KEY, itemId, (id) -> {
                return null;
            }).values();
            for (Item item : blocks) {
                sum += handler.getStat(Stats.USED, item);
            }
            return sum;
        }
    }
}
