package douplo.item;

import com.google.gson.JsonObject;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.Set;

public class ServerToolItem extends ToolItem implements ServerOnlyItem {

    private final int modelId;
    private final Identifier id;

    private final ItemData itemData;

    public static final Serializer<ServerToolItem> SERIALIZER = new Serializer<ServerToolItem>() {
        @Override
        public ServerToolItem fromJson(Identifier id, JsonObject json, ItemData data) {

            Identifier materialId = new Identifier(json.get("material").getAsString());
            ToolMaterial material = LoadedMaterial.getById(materialId).getToolMaterial();

            return new ServerToolItem(id, material, data.settings, data);
        }
    };

    /*public ServerToolItem(Identifier id, ToolMaterial material, Item.Settings settings) {
        super(material, settings.maxDamageIfAbsent(material.getDurability()));
        this.id = id;
        this.modelId = ServerOnlyItem.registerModelId(this);
        ServerOnlyItem.registerServerOnlyItem(id, this);
    }*/

    public ServerToolItem(Identifier id, ToolMaterial material, Item.Settings settings, ItemData data) {
        super(material, settings.maxDamageIfAbsent(material.getDurability()));
        this.itemData = data;
        this.modelId = ServerOnlyItem.registerModelId(this);
        this.id = id;
        ServerOnlyItem.registerServerOnlyItem(id, this);
    }

    public JsonObject getModelData() {
        JsonObject modelData = new JsonObject();
        modelData.addProperty("parent", "item/handheld");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", this.getId().getNamespace() + ":item/" + this.getId().getPath());
        modelData.add("textures", textures);
        return modelData;
    }

    @Override
    public Identifier getTextureId() {
        return itemData.textureId;
    }

    @Override
    public Set<ResourceIdentifier> getExtraResources() {
        return itemData.extraResources;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public Item getClientItem() {
        return itemData.clientItem;
    }

    @Override
    public int getModelId() {
        return modelId;
    }

    @Override
    public int getMaxDamageForClient() {
        return getMaxDamage();
    }

    @Override
    public Optional<Identifier> getCustomModelId() {
        return itemData.modelId;
    }

    public Item asItem() {
        return this;
    }

    public String getDisplayNameForLangFile() {
        return itemData.displayName;
    }

}
