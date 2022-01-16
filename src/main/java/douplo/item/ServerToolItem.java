package douplo.item;

import com.google.gson.JsonObject;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public class ServerToolItem extends ToolItem implements ServerOnlyItem {

    private final Item clientItem;
    private final int modelId;
    private final Identifier id;

    private Optional<Identifier> customModelId = Optional.empty();
    private Identifier textureId;

    public static final Serializer<ServerToolItem> SERIALIZER = new Serializer<ServerToolItem>() {
        @Override
        public ServerToolItem fromJson(Identifier id, JsonObject json, DeserializationData data) {

            Identifier materialId = new Identifier(json.get("material").getAsString());
            ToolMaterial material = LoadedMaterial.getById(materialId);

            return new ServerToolItem(id, material, data.settings, data.clientItem);
        }
    };

    public ServerToolItem(Identifier id, ToolMaterial material, Item.Settings settings) {
        super(material, settings.maxDamageIfAbsent(material.getDurability()));
        this.id = id;
        this.clientItem = Items.CARROT_ON_A_STICK;
        this.modelId = ServerOnlyItem.registerModelId(this);
        ServerOnlyItem.registerServerOnlyItem(id, this);
    }

    public ServerToolItem(Identifier id, ToolMaterial material, Item.Settings settings, Item clientItem) {
        super(material, settings.maxDamageIfAbsent(material.getDurability()));
        this.clientItem = clientItem;
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
        return textureId;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public Item getClientItem() {
        return clientItem;
    }

    @Override
    public int getModelId() {
        return modelId;
    }

    @Override
    public Optional<Identifier> getCustomModelId() {
        return customModelId;
    }

    public Item asItem() {
        return this;
    }
}
