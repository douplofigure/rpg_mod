package douplo.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.*;

public class GenericServerItem extends Item implements ServerOnlyItem {

    private final int modelId;
    private final Identifier id;

    private final ItemData itemData;


    public static final Serializer<GenericServerItem> GENERIC_SERIALIZER = new Serializer<GenericServerItem>() {
        @Override
        public GenericServerItem fromJson(Identifier id, JsonObject json, ItemData data) {
            return new GenericServerItem(id, data.settings, data);
        }
    };

    public GenericServerItem(Identifier id, Settings settings, ItemData data) {
        super(settings);
        this.id = id;
        this.itemData = data;
        modelId = ServerOnlyItem.registerModelId(this);
        ServerOnlyItem.registerServerOnlyItem(id, this);
    }

    public Item getClientItem() {
        return this.itemData.clientItem;
    }

    @Override
    public int getModelId() {
        return modelId;
    }

    @Override
    public Optional<Identifier> getCustomModelId() {
        return itemData.modelId;
    }

    @Override
    public Identifier getTextureId() {
        return itemData.textureId;
    }

    @Override
    public Set<ResourceIdentifier> getExtraResources() {
        return itemData.extraResources;
    }

    public Identifier getId() {
        return this.id;
    }

    @Override
    public String getDisplayNameForLangFile() {
        return itemData.displayName;
    }

    public boolean isNbtSynced() {
        return true;
    }

    public Item asItem() {
        return this;
    }

}
