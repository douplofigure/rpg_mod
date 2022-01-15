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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;

public class ServerOnlyItem extends Item {

    public static final String SERVER_TEXTURE_DIR = "server_textures";
    public static final String SERVER_MODEL_DIR = "server_models";

    private static Map<Identifier, Type<?>> registeredTypes = new HashMap<>();

    private final Item clientItem;
    private final int modelId;
    private final Identifier id;
    private static Map<Item, List<ServerOnlyItem>> modelIds = new HashMap<>();
    private static Map<Identifier, ServerOnlyItem> serverOnlyItemMap = new HashMap<>();

    private Optional<Identifier> customModelId = Optional.empty();
    private Identifier textureId;

    public static class DeserializationData {
        Settings settings;
        Item clientItem;
        Identifier textureId;
        Optional<Identifier> modelId;
    }

    public interface Serializer<T extends ServerOnlyItem> {
        public T fromJson(Identifier id, JsonObject json, DeserializationData data);
    }

    public static final Serializer<ServerOnlyItem> GENERIC_SERIALIZER = new Serializer<ServerOnlyItem>() {
        @Override
        public ServerOnlyItem fromJson(Identifier id, JsonObject json, DeserializationData data) {
            return new ServerOnlyItem(id, data.settings, data.clientItem);
        }
    };

    public static class Type<T extends ServerOnlyItem> {

         private final Serializer<T> serializer;

        public Type(Serializer<T> serializer) {
            this.serializer = serializer;
        }

        public Serializer<T> getSerializer() {
            return serializer;
        }

    }

    protected static <T extends ServerOnlyItem> Type<T> registerType(Identifier typeId, Serializer<T> serializer) {
        return registerType(typeId, new Type<>(serializer));
    }

    protected static <T extends ServerOnlyItem> Type<T> registerType(Identifier typeId, Type<T> type) {
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
        NbtList modifiers = new NbtList();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Multimap<EntityAttribute, EntityAttributeModifier> mods = stack.getAttributeModifiers(slot);
            if (!mods.isEmpty()) {
                writtenModifiers = true;
                for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : mods.entries()) {
                    NbtCompound entryTag = entry.getValue().toNbt();
                    entryTag.putString("AttributeName", Registry.ATTRIBUTE.getId(entry.getKey()).toString());
                    entryTag.putString("Slot", slot.getName());
                    modifiers.add(entryTag);
                }
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

        tag.putByte("ServerOnlyItem", (byte) 1);

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

                Identifier serverModelId;
                if (serverItem.customModelId.isEmpty()) {
                    serverModelId = new Identifier(serverItem.id.getNamespace(), "item/" + serverItem.id.getPath());

                    JsonObject serverModelData = serverItem.getModelData();
                    modelMap.put(serverModelId, serverModelData);

                } else {
                    serverModelId = serverItem.customModelId.get();
                }

                JsonObject clientOverride = new JsonObject();
                clientOverride.add("predicate", createCustomModelDataPredicate(serverItem.modelId));
                clientOverride.addProperty("model", serverModelId.toString());

                overrides.add(clientOverride);

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
        if (this.textureId != null) {
            textures.addProperty("layer0", this.textureId.toString());
        } else {
            textures.addProperty("layer0", this.id.getNamespace() + ":item/" + this.id.getPath());
        }
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

        if (json.has("model")) {
            data.textureId = null;
            data.modelId = Optional.of(new Identifier(json.get("model").getAsString()));
        } else {
            data.textureId = new Identifier(json.get("texture").getAsString());
            data.modelId = Optional.empty();
        }

        Identifier typeId = new Identifier(json.get("type").getAsString());

        return registeredTypes.get(typeId).serializer.fromJson(id, json, data);

    }

    public static class ResourceIdentifier extends Identifier {

        enum ResourceType {
            TEXTURE,
            MODEL,
        }

        private final ResourceType type;

        public ResourceIdentifier(Identifier id, ResourceType type) {
            this(id.getNamespace(), id.getPath(), type);
        }

        public ResourceIdentifier(String namespace, String path, ResourceType type) {
            super(namespace, path);
            this.type = type;
        }

        public ResourceType getType() {
            return type;
        }

        public Identifier getFilePath() {
            switch (type) {
                case MODEL:
                    return new Identifier(getNamespace(), SERVER_MODEL_DIR + "/" + getPath() + ".json");
                case TEXTURE:
                    return new Identifier(getNamespace(), SERVER_TEXTURE_DIR + "/" + getPath() + ".png");
            }
            return null;
        }

        public Identifier getDestinationPath() {
            switch (type) {
                case MODEL:
                    return new Identifier(getNamespace(), "models/" + getPath() + ".json");
                case TEXTURE:
                    return new Identifier(getNamespace(), "textures/" + getPath() + ".png");
            }
            return null;
        }

    }

    protected List<ResourceIdentifier> getResources() {
        List<ResourceIdentifier> resources = new LinkedList<>();
        if (this.customModelId.isPresent()) {
            resources.add(new ResourceIdentifier(this.customModelId.get(), ResourceIdentifier.ResourceType.MODEL));
        } else if (this.textureId != null) {
            resources.add(new ResourceIdentifier(textureId, ResourceIdentifier.ResourceType.TEXTURE));
        } else {
            resources.add(new ResourceIdentifier(this.id.getNamespace(), "item/"+this.id.getPath(), ResourceIdentifier.ResourceType.TEXTURE));
        }
        return resources;
    }

    public static List<ResourceIdentifier> getExtraResources() {

        List<ResourceIdentifier> resources = new LinkedList<>();
        for (Map.Entry<Item, List<ServerOnlyItem>> entry : modelIds.entrySet()) {
            for (ServerOnlyItem item : entry.getValue()) {
                resources.addAll(item.getResources());
            }
        }

        return resources;

    }

    public static ServerOnlyItem getFromItemAndModel(Item item, int modelId) {
        List<ServerOnlyItem> ls = modelIds.get(item);
        return ls.get(modelId-1);
    }

}
