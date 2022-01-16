package douplo.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.*;

public class GenericServerItem extends Item implements ServerOnlyItem {

    private final Item clientItem;
    private final int modelId;
    private final Identifier id;

    private Optional<Identifier> customModelId = Optional.empty();
    private Identifier textureId;


    public static final Serializer<GenericServerItem> GENERIC_SERIALIZER = new Serializer<GenericServerItem>() {
        @Override
        public GenericServerItem fromJson(Identifier id, JsonObject json, DeserializationData data) {
            return new GenericServerItem(id, data.settings, data.clientItem);
        }
    };

    public GenericServerItem(Identifier id, Settings settings) {
        super(settings);
        this.id = id;
        this.clientItem = ServerOnlyItem.getDefaultClientItem(this.getMaxCount());
        modelId = ServerOnlyItem.registerModelId(this);
        ServerOnlyItem.registerServerOnlyItem(id, this);
    }

    public GenericServerItem(Identifier id, Settings settings, Item clientItem) {
        super(settings);
        this.id = id;
        this.clientItem = clientItem;
        modelId = ServerOnlyItem.registerModelId(this);
        ServerOnlyItem.registerServerOnlyItem(id, this);
    }

    public Item getClientItem() {
        return this.clientItem;
    }

    @Override
    public int getModelId() {
        return modelId;
    }

    @Override
    public Optional<Identifier> getCustomModelId() {
        return customModelId;
    }

    @Override
    public Identifier getTextureId() {
        return textureId;
    }

    public Identifier getId() {
        return this.id;
    }

    public boolean isNbtSynced() {
        return true;
    }

    public Item asItem() {
        return this;
    }

}
