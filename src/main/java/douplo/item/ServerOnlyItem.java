package douplo.item;

import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import douplo.RpgMod;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerOnlyItem extends Item {

    private static Map<Identifier, Type<?>> registeredTypes = new HashMap<>();

    private final Item clientItem;
    private final int modelId;
    private final Identifier id;
    private static Map<Item, List<ServerOnlyItem>> modelIds = new HashMap<>();
    private static Map<Identifier, ServerOnlyItem> serverOnlyItemMap = new HashMap<>();

    public static class DeserializationData {
        Settings settings;
        Item clientItem;
    }

    public interface Serializer<T extends ServerOnlyItem> {
        public T fromJson(Identifier id, JsonObject json, DeserializationData data);
    }

    private static final Serializer<ServerOnlyItem> GENERIC_SERIALIZER = new Serializer<ServerOnlyItem>() {
        @Override
        public ServerOnlyItem fromJson(Identifier id, JsonObject json, DeserializationData data) {
            return new ServerOnlyItem(id, data.settings, data.clientItem);
        }
    };

    public static final Type<ServerOnlyItem> GENERIC = registerType(new Identifier(RpgMod.MODID, "generic"), GENERIC_SERIALIZER);

    public static class Type<T extends ServerOnlyItem> {

         private final Serializer<T> serializer;

        public Type(Serializer<T> serializer) {
            this.serializer = serializer;
        }

        public Serializer<T> getSerializer() {
            return serializer;
        }

    }

    private static <T extends ServerOnlyItem> Type<T> registerType(Identifier typeId, Serializer<T> serializer) {
        return registerType(typeId, new Type<>(serializer));
    }

    private static <T extends ServerOnlyItem> Type<T> registerType(Identifier typeId, Type<T> type) {
        registeredTypes.put(typeId, type);
        return type;
    }

    private static Item getDefaultClientItem(int maxCount) {
        if (maxCount == 1) {
            return Items.CARROT_ON_A_STICK;
        } else if (maxCount == 16) {
            return Items.ENDER_PEARL;
        } else {
            return Items.SLIME_BALL;
        }
    }

    public ServerOnlyItem(Identifier id, Settings settings) {
        super(settings);
        this.id = id;
        this.clientItem = getDefaultClientItem(this.getMaxCount());
        modelId = registerModelId(this);
        registerServerOnlyItem(id, this);
    }

    private void registerServerOnlyItem(Identifier id, ServerOnlyItem serverOnlyItem) {
        serverOnlyItemMap.put(id, serverOnlyItem);
    }

    public ServerOnlyItem(Identifier id, Settings settings, Item clientItem) {
        super(settings);
        this.id = id;
        this.clientItem = clientItem;
        modelId = registerModelId(this);
        registerServerOnlyItem(id, this);
    }

    public Item getClientItem() {
        return this.clientItem;
    }

    public Identifier getId() {
        return this.id;
    }

    public boolean isNbtSynced() {
        return true;
    }

    public NbtCompound getEncodedClientData(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag == null)
            tag = new NbtCompound();

        tag.putInt("CustomModelData", modelId);

        if (this.getMaxDamage() > 0) {
            int clientDamage = stack.getDamage() * clientItem.getMaxDamage() / this.getMaxDamage();
            tag.putInt("Damage", clientDamage);
        }

        boolean writtenModifiers = false;
        Multimap<EntityAttribute, EntityAttributeModifier> mods = this.getAttributeModifiers(EquipmentSlot.MAINHAND);
        NbtList modifiers = new NbtList();
        if (!mods.isEmpty()) {
            writtenModifiers = true;
            for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : mods.entries()) {
                NbtCompound entryTag = entry.getValue().toNbt();
                modifiers.add(entryTag);
            }
        }

        if (writtenModifiers) {
            tag.put("AttributeModifiers", modifiers);
        }

        NbtCompound displayTag = new NbtCompound();
        JsonObject translate = new JsonObject();
        translate.addProperty("translate", this.getTranslationKey());
        translate.addProperty("italic", false);
        displayTag.putString("Name", translate.toString());
        tag.put("display", displayTag);

        RpgMod.LOGGER.info("Sending tag: " + tag + " for item " + this);

        return tag;
    }

    private static int registerModelId(ServerOnlyItem serverOnlyItem) {

        Item clientItem = serverOnlyItem.getClientItem();
        if (!modelIds.containsKey(clientItem))
            modelIds.put(clientItem, new ArrayList<>());

        int modelId = modelIds.get(clientItem).size() + 1;
        modelIds.get(clientItem).add(serverOnlyItem);
        return modelId;

    }

    public static Map<Identifier, JsonObject> generateModelOverrides() {

        Map<Identifier, JsonObject> modelMap = new HashMap<>();

        for (Map.Entry<Item, List<ServerOnlyItem>> entry : modelIds.entrySet()) {

            Item clientItem = entry.getKey();
            Identifier clientItemId = Registry.ITEM.getId(clientItem);
            Identifier clientModelId = new Identifier(clientItemId.getNamespace(), "item/" + clientItemId.getPath());

            JsonObject clientModelData = new JsonObject();
            clientModelData.addProperty("parent", clientItem.getMaxCount() == 1 ? "item/handheld" : "item/generated");
            JsonObject clientModelTextures = new JsonObject();
            clientModelTextures.addProperty("layer0", clientModelId.toString());
            clientModelData.add("textures", clientModelTextures);

            JsonArray overrides = new JsonArray();
            for (ServerOnlyItem serverItem : entry.getValue()) {

                Identifier serverModelId = new Identifier(serverItem.id.getNamespace(), "item/" + serverItem.id.getPath());

                JsonObject clientOverride = new JsonObject();
                clientOverride.add("predicate", createCustomModelDataPredicate(serverItem.modelId));
                clientOverride.addProperty("model", serverModelId.toString());

                overrides.add(clientOverride);

                JsonObject serverModelData = serverItem.getModelData();

                modelMap.put(serverModelId, serverModelData);

            }

            if (overrides.size() > 0) {
                clientModelData.add("overrides", overrides);
            }

            modelMap.put(clientModelId, clientModelData);

        }

        return modelMap;

    }

    protected JsonObject getModelData() {
        JsonObject modelData = new JsonObject();
        modelData.addProperty("parent", "item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", this.id.getNamespace() + ":item/" + this.id.getPath());
        modelData.add("textures", textures);
        return modelData;
    }

    private static JsonElement createCustomModelDataPredicate(int modelId) {
        JsonObject predicate = new JsonObject();
        predicate.addProperty("custom_model_data", modelId);
        return predicate;
    }

    public static void clearCache() {
        serverOnlyItemMap.clear();
        modelIds.clear();
    }

    public static ServerOnlyItem fromJson(Identifier id, JsonObject json) {

        DeserializationData data = new DeserializationData();
        data.settings = new Settings();

        int maxCount = 64;
        if (json.has("settings")) {
            JsonObject settings = json.getAsJsonObject("settings");
            if (settings.has("max_count")) {
                maxCount = settings.get("max_count").getAsInt();
                data.settings.maxCount(maxCount);
            }
            if (settings.has("max_damage")) {
                data.settings.maxDamage(settings.get("max_damage").getAsInt());
            }
        }

        if (json.has("client_item")) {
            Identifier clientItemId = new Identifier(json.get("client_item").getAsString());
            data.clientItem = Registry.ITEM.get(clientItemId);
        } else {
            data.clientItem = getDefaultClientItem(maxCount);
        }

        Identifier typeId = new Identifier(json.get("type").getAsString());

        return registeredTypes.get(typeId).serializer.fromJson(id, json, data);

    }

}
