package douplo.skill;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;


public class MiningSkill extends Skill{

    private final boolean isTag;
    private final Identifier blockId;

    public static final Serializer<MiningSkill> SERIALIZER = new Serializer<MiningSkill>() {
        @Override
        public MiningSkill read(JsonObject json, Identifier identifier, Text displayName) {
            if (json.has("tag")) {
                Identifier tagId = new Identifier(json.get("tag").getAsString());
                return new MiningSkill(identifier, displayName, tagId, true);
            } else {
                Identifier blockId = new Identifier(json.get("block").getAsString());
                return new MiningSkill(identifier, displayName, blockId);
            }
        }

        @Override
        public void write(MiningSkill skill, JsonObject jsonObject) {

        }
    };

    public MiningSkill(Identifier id, Text displayName, Identifier block) {
        super(id, displayName);
        this.blockId = block;
        this.isTag = false;
    }

    public MiningSkill(Identifier id, Text displayName, Identifier block, boolean tag) {
        super(id, displayName);
        this.blockId = block;
        this.isTag = tag;
    }

    @Override
    protected double getRawValue(PlayerEntity player) {
        StatHandler handler = ((ServerPlayerEntity)player).getStatHandler();
        if (!this.isTag)
            return handler.getStat(Stats.MINED, Registry.BLOCK.get(blockId));
        else {
            double sum = 0.0;
            List<Block> blocks = player.getWorld().getServer().getTagManager().getTag(Registry.BLOCK_KEY, blockId, (id) -> {
                return null;
            }).values();
            for (Block b : blocks) {
                sum += handler.getStat(Stats.MINED, b);
            }
            return sum;
        }
    }
}
