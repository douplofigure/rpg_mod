package douplo.playerclass;

import douplo.RpgMod;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.util.*;

public class PlayerClass {

    private static Map<Identifier, PlayerClass> registeredClasses = new HashMap<>();

    private final Identifier id;
    private Text displayName;
    private Map<Identifier, Double> skillMultipliers = new HashMap<>();
    private Map<EntityAttribute, Double> attributeValues = new HashMap<>();

    public PlayerClass(Identifier id) {
        this.id = id;

        registeredClasses.put(id, this);

    }

    public PlayerClass(Identifier id, Text displayName) {
        this(id);
        this.displayName = displayName;
    }

    public static void registerDefaultClass() {
        registeredClasses.put(PlayerClassMap.DEFAULT.getId(), PlayerClassMap.DEFAULT);
    }

    public Text getDisplayName() {
        return displayName;
    }

    public void setDisplayName(Text displayName) {
        this.displayName = displayName;
    }

    public static PlayerClass getFromId(Identifier id) {
        return registeredClasses.get(id);
    }

    public static Collection<PlayerClass> getLoadedClasses() {
        return registeredClasses.values();
    }

    public static Set<Identifier> getLoadedClassIds() {
        return registeredClasses.keySet();
    }

    public static void clearCache() {
        registeredClasses.clear();
    }

    public Identifier getId() {
        return id;
    }

    public double getSkillMultiplier(Identifier id) {
        if (skillMultipliers.containsKey(id))
            return skillMultipliers.get(id);
        RpgMod.LOGGER.warn("PlayerClass " + this.id + " has no multiplier for skill " + id);
        return 1.0;
    }

    public void setSkillMultiplier(Identifier skillId, double value) {
        RpgMod.LOGGER.info("Setting multiplier for skill " + skillId + " to " + value + " for class " + id);
        skillMultipliers.put(skillId, value);
    }

    public void onPlayerSet(PlayerEntity player) {

        try {
            for (Map.Entry<RegistryKey<EntityAttribute>, EntityAttribute> entry : Registry.ATTRIBUTE.getEntries()) {
                EntityAttributeInstance instance = player.getAttributeInstance(entry.getValue());
                if (instance == null)
                    continue;
                double value = entry.getValue().getDefaultValue();
                if (entry.getValue() == EntityAttributes.GENERIC_MOVEMENT_SPEED)
                    value = 0.1;
                instance.setBaseValue(value);
            }
        } catch (Exception e) {
            RpgMod.LOGGER.error(e);
        }


        for (Map.Entry<EntityAttribute, Double> e : attributeValues.entrySet()) {
            player.getAttributeInstance(e.getKey()).setBaseValue(e.getValue());
        }

    }

    public void setAttribute(Identifier attrId, double value) {

        EntityAttribute attr = Registry.ATTRIBUTE.get(attrId);
        attributeValues.put(attr, value);

    }

    public Set<EntityAttribute> getModifiedAttributes() {
        return attributeValues.keySet();
    }

    public double getAttributeValue(EntityAttribute attr) {
        return attributeValues.get(attr);
    }
}
