package douplo.skill.bonus;

import com.google.gson.*;
import douplo.RpgMod;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.JsonSerializing;
import net.minecraft.util.registry.Registry;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillBonus {

    private static Map<Identifier, SkillBonusType<?>> registeredTypes = new HashMap<>();
    private static Map<Identifier, SkillBonus> loadedBonuses = new HashMap<>();
    public static final SkillBonus.SkillBonusType<SimpleValueSkillBonus> MINING_SPEED = (SkillBonus.SkillBonusType<SimpleValueSkillBonus>) SkillBonus.registerType(new Identifier(RpgMod.MODID, "mining_speed"), new SkillBonus.SkillBonusType<SimpleValueSkillBonus>() {
        @Override
        public SimpleValueSkillBonus load(JsonObject json, Gson gson) {
            LootNumberProvider provider = gson.fromJson(json.get("value"), LootNumberProvider.class);
            return new SimpleValueSkillBonus(provider);
        }
    });

    public static abstract class SkillBonusType<T extends SkillBonus> {

        private List<T> loadedModifiers = new ArrayList<>();

        public abstract T load(JsonObject json, Gson gson);

        public T fromJson(JsonObject json, Gson gson) {
            T obj = load(json, gson);
            loadedModifiers.add(obj);
            return obj;
        }

        private List<T> getLoadedModifiers() {
            return loadedModifiers;
        }

        private void clearCache() {
            loadedModifiers.clear();
        }

    }

    public SkillBonus() {

    }

    public static <T extends SkillBonus> List<T> getAllOfType(SkillBonusType<T> type) {
        return type.getLoadedModifiers();
    }

    public static SkillBonusType<?> registerType(Identifier id, SkillBonusType<?> type) {
        registeredTypes.put(id, type);
        return type;
    }

    public static SkillBonus fromJson(JsonObject json, Gson gson) {
        Identifier typeId = new Identifier(json.get("type").getAsString());
        SkillBonusType type = registeredTypes.get(typeId);
        return type.fromJson(json, gson);
    }

    public static SkillBonus loadFromFile(Identifier id, JsonObject json, Gson gson) {
        SkillBonus bonus = fromJson(json, gson);
        loadedBonuses.put(id, bonus);
        return bonus;
    }

    public static SkillBonus loadFromFile(Identifier id, InputStream stream) {

        Object serializer = JsonSerializing.createSerializerBuilder(Registry.LOOT_NUMBER_PROVIDER_TYPE, "provider", "type", LootNumberProvider::getType).elementSerializer(LootNumberProviderTypes.CONSTANT, new ConstantLootNumberProvider.CustomSerializer()).defaultType(LootNumberProviderTypes.UNIFORM).build();
        GsonBuilder builder = new GsonBuilder().registerTypeHierarchyAdapter(LootNumberProvider.class, LootNumberProviderTypes.createGsonSerializer());
        Gson gson = builder.create();
        JsonObject object = gson.fromJson(new InputStreamReader(stream), JsonObject.class);
        return loadFromFile(id, object, gson);
    }

    public static void clearCache() {
        for (Map.Entry<Identifier, SkillBonusType<?>> typeEntry : registeredTypes.entrySet()) {
            typeEntry.getValue().clearCache();
        }
        loadedBonuses.clear();
    }

}
