package douplo.item;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class LoadedMaterial implements ToolMaterial, ArmorMaterial {

    private final int durability;
    private final float miningSpeedMultiplier;
    private final float attackDamage;
    private final int miningLevel;
    private final int enchantability;
    private final Ingredient repairIngredient;

    private static Map<Identifier, LoadedMaterial> toolMaterialMap = new HashMap<>();
    private final int durabilityMultiplier;
    private final int[] protectionAmounts;
    private final String name;
    private final float toughness;
    private final float knockbackResistance;

    private static final int[] BASE_DURABILITY;

    static {
        BASE_DURABILITY = new int[]{13, 15, 16, 11};
    }




    public LoadedMaterial(int durability, float miningSpeedMultiplier, float attackDamage, int miningLevel, int enchantability, Ingredient repairIngredient, int durabilityMultiplier, int[] protectionAmounts, String name, float toughness, float knockbackResistance) {
        this.durability = durability;
        this.miningSpeedMultiplier = miningSpeedMultiplier;
        this.attackDamage = attackDamage;
        this.miningLevel = miningLevel;
        this.enchantability = enchantability;
        this.repairIngredient = repairIngredient;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionAmounts = protectionAmounts;
        this.name = name;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
    }

    @Override
    public int getDurability() {
        return durability;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return miningSpeedMultiplier;
    }

    @Override
    public float getAttackDamage() {
        return attackDamage;
    }

    @Override
    public int getMiningLevel() {
        return miningLevel;
    }

    @Override
    public int getDurability(EquipmentSlot slot) {
        return BASE_DURABILITY[slot.getEntitySlotId()] * this.durabilityMultiplier;
    }

    @Override
    public int getProtectionAmount(EquipmentSlot slot) {
        return protectionAmounts[slot.getEntitySlotId()];
    }

    @Override
    public int getEnchantability() {
        return enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_IRON;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

    public static LoadedMaterial fromJson(JsonObject json) {
        int durability = json.get("durability").getAsInt();
        float miningSpeed = json.get("mining_speed").getAsInt();
        float attackDamage = json.get("attack_damage").getAsInt();
        int miningLevel = json.get("mining_level").getAsInt();
        Ingredient repair = Ingredient.fromJson(json.get("repair_ingredient"));
        int enchantability = json.get("enchantability").getAsInt();

        int durabilityMultiplier = json.get("armor_durability").getAsInt();
        int[] protectionAmounts = new Gson().fromJson(json.get("protection_amounts"), int[].class);
        String name = json.get("name").getAsString();
        float toughness = json.get("armor_toughness").getAsFloat();
        float knockbackResistance = json.get("knockback_resistance").getAsFloat();

        return new LoadedMaterial(durability, miningSpeed, attackDamage, miningLevel, enchantability, repair, durabilityMultiplier, protectionAmounts, name, toughness, knockbackResistance);
    }

    public static LoadedMaterial register(Identifier id, LoadedMaterial material) {
        toolMaterialMap.put(id, material);
        return material;
    }

    public static LoadedMaterial loadFromJson(Identifier id, JsonObject object) {
        return register(id, fromJson(object));
    }

    public static LoadedMaterial getById(Identifier id) {
        return toolMaterialMap.get(id);
    }

}
