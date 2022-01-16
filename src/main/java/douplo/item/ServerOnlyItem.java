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
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;

public interface ServerOnlyItem extends ItemConvertible {

    public static final String SERVER_TEXTURE_DIR = "server_textures";
    public static final String SERVER_MODEL_DIR = "server_models";
    public static final String SERVER_EXTRA_DIR = "extra_resources";

    static Map<Item, List<ServerOnlyItem>> modelIds = new HashMap<>();
    static Map<Identifier, ServerOnlyItem> serverOnlyItemMap = new HashMap<>();

    static Map<Identifier, Type<?>> registeredTypes = new HashMap<>();

    public static class DeserializationData {
        Item.Settings settings;
        Item clientItem;
        Identifier textureId;
        Optional<Identifier> modelId;
    }


    public static class ResourceIdentifier extends Identifier {

        enum ResourceType {
            TEXTURE,
            MODEL,
            GENERIC_FILE,
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
                case GENERIC_FILE:
                    return new Identifier(getNamespace(), SERVER_EXTRA_DIR + "/" +getPath());
            }
            return null;
        }

        public Identifier getDestinationPath() {
            switch (type) {
                case MODEL:
                    return new Identifier(getNamespace(), "models/" + getPath() + ".json");
                case TEXTURE:
                    return new Identifier(getNamespace(), "textures/" + getPath() + ".png");
                case GENERIC_FILE:
                    return new Identifier(getNamespace(), getPath());
            }
            return null;
        }

        public int hashCode() {
            return 127 * this.namespace.hashCode() + 3 * this.path.hashCode() + this.type.hashCode();
        }

        public int compareTo(Identifier identifier) {
            if (!(identifier instanceof ResourceIdentifier)) {
                return -1;
            }
            if (((ResourceIdentifier)identifier).type == this.type) {

                int i = this.path.compareTo(identifier.getPath());
                if (i == 0) {
                    i = this.namespace.compareTo(identifier.getNamespace());
                }
                return i;
            }
            return this.type.hashCode() - ((ResourceIdentifier)identifier).type.hashCode();
        }

    }

    public static interface Serializer<T extends ServerOnlyItem> {
        public T fromJson(Identifier id, JsonObject json, DeserializationData data);
    }

    public static class Type<T extends ServerOnlyItem> {

        private final Serializer<T> serializer;

        public Type(Serializer<T> serializer) {
            this.serializer = serializer;
        }

        public Serializer<T> getSerializer() {
            return serializer;
        }

    }

    static <T extends ServerOnlyItem> Type<T> registerType(Identifier typeId, Serializer<T> serializer) {
        return registerType(typeId, new Type<>(serializer));
    }

    static <T extends ServerOnlyItem> Type<T> registerType(Identifier typeId, Type<T> type) {
        registeredTypes.put(typeId, type);
        return type;
    }

    static Item getDefaultClientItem(int maxCount) {
        if (maxCount == 1) {
            return Items.CARROT_ON_A_STICK;
        } else if (maxCount == 16) {
            return Items.ENDER_PEARL;
        } else {
            return Items.SLIME_BALL;
        }
    }

    default Set<ResourceIdentifier> getResources() {
        Set<ResourceIdentifier> resources = new HashSet<>();
        if (this.getCustomModelId().isPresent()) {
            resources.add(new ResourceIdentifier(this.getCustomModelId().get(), ResourceIdentifier.ResourceType.MODEL));
        } else if (this.getTextureId() != null) {
            resources.add(new ResourceIdentifier(getTextureId(), ResourceIdentifier.ResourceType.TEXTURE));
        } else {
            resources.add(new ResourceIdentifier(this.getId().getNamespace(), "item/"+this.getId().getPath(), ResourceIdentifier.ResourceType.TEXTURE));
        }
        return resources;
    }

    Identifier getTextureId();

    Identifier getId();

    public static Set<ResourceIdentifier> getExtraResources() {

        Set<ResourceIdentifier> resources = new HashSet<>();
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

    static JsonElement createCustomModelDataPredicate(int modelId) {
        JsonObject predicate = new JsonObject();
        predicate.addProperty("custom_model_data", modelId);
        return predicate;
    }

    public static void clearCache() {
        serverOnlyItemMap.clear();
        modelIds.clear();
    }

    public Item getClientItem();
    public int getModelId();
    public int getMaxDamage();
    public default String getTranslationKey() {
        return "item."+getId().getNamespace() +"."+ getId().getNamespace();
    }
    Optional<Identifier> getCustomModelId();

    default JsonObject getModelData()  {
        JsonObject modelData = new JsonObject();
        modelData.addProperty("parent", "item/generated");
        JsonObject textures = new JsonObject();
        if (this.getTextureId() != null) {
            textures.addProperty("layer0", this.getTextureId().toString());
        } else {
            textures.addProperty("layer0", this.getId().getNamespace() + ":item/" + this.getId().getPath());
        }
        modelData.add("textures", textures);
        return modelData;
    }

    public default NbtCompound getEncodedClientData(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag == null)
            tag = new NbtCompound();

        tag.putInt("CustomModelData", getModelId());

        if (this.getMaxDamage() > 0) {
            int clientDamage = stack.getDamage() * getClientItem().getMaxDamage() / this.getMaxDamage();
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

        return tag;
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
                if (serverItem.getCustomModelId().isEmpty()) {
                    serverModelId = new Identifier(serverItem.getId().getNamespace(), "item/" + serverItem.getId().getPath());

                    JsonObject serverModelData = serverItem.getModelData();
                    modelMap.put(serverModelId, serverModelData);

                } else {
                    serverModelId = serverItem.getCustomModelId().get();
                }

                JsonObject clientOverride = new JsonObject();
                clientOverride.add("predicate", ServerOnlyItem.createCustomModelDataPredicate(serverItem.getModelId()));
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

    public static ServerOnlyItem fromJson(Identifier id, JsonObject json) {

        DeserializationData data = new DeserializationData();
        data.settings = new Item.Settings();

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
            data.clientItem = ServerOnlyItem.getDefaultClientItem(maxCount);
        }

        if (json.has("model")) {
            data.textureId = null;
            data.modelId = Optional.of(new Identifier(json.get("model").getAsString()));
        } else {
            data.textureId = new Identifier(json.get("texture").getAsString());
            data.modelId = Optional.empty();
        }

        Identifier typeId = new Identifier(json.get("type").getAsString());

        return registeredTypes.get(typeId).getSerializer().fromJson(id, json, data);
    }

    static int registerModelId(ServerOnlyItem serverOnlyItem) {

        Item clientItem = serverOnlyItem.getClientItem();
        if (clientItem == null)
            throw new NullPointerException("NULL-client-item");
        if (!modelIds.containsKey(clientItem))
            modelIds.put(clientItem, new ArrayList<>());

        int modelId = modelIds.get(clientItem).size() + 1;
        modelIds.get(clientItem).add(serverOnlyItem);
        return modelId;

    }

    static void registerServerOnlyItem(Identifier id, ServerOnlyItem serverOnlyItem) {
        serverOnlyItemMap.put(id, serverOnlyItem);
    }

}
