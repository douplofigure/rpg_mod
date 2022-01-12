package douplo.skill;

import douplo.RpgMod;
import net.minecraft.util.Identifier;

public class SkillTypes {

    public static final SkillType CRAFTING = register(new Identifier(RpgMod.MODID, "crafting"), CraftingSkill.SERIALIZER);
    public static final SkillType COMBINED = register(new Identifier(RpgMod.MODID, "combined"), CombinedSkill.SERIALIZER);
    public static final SkillType STAT = register(new Identifier(RpgMod.MODID, "stat"), StatSkill.SERIALIZER);
    public static final SkillType REFERENCE = register(new Identifier(RpgMod.MODID, "reference"), ReferenceSkill.SERIALIZER);
    public static final SkillType MINING = register(new Identifier(RpgMod.MODID, "mining"), MiningSkill.SERIALIZER);
    public static final SkillType USING = register(new Identifier(RpgMod.MODID, "using"), UseSkill.SERIALIZER);

    public static <T extends Skill> SkillType<T> register(Identifier id, Skill.Serializer<T> serializer) {
        return new SkillType<T>(id, serializer);
    }

}
