package douplo.item;


import com.google.gson.JsonObject;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ServerArmorItem extends ArmorItem implements ServerOnlyItem {

    private final Identifier id;
    private final Item clientItem;
    private final int modelId;

    public static final ServerOnlyItem.Serializer<ServerArmorItem> SERIALIZER = new ServerOnlyItem.Serializer<ServerArmorItem>() {
        @Override
        public ServerArmorItem fromJson(Identifier id, JsonObject json, ServerOnlyItem.DeserializationData data) {
            ArmorMaterial material = LoadedMaterial.getById(new Identifier(json.get("material").getAsString()));
            EquipmentSlot slot = EquipmentSlot.byName(json.get("slot").getAsString());
            return new ServerArmorItem(id, material, slot, data.settings, data.clientItem);
        }
    };

    public ServerArmorItem(Identifier id, ArmorMaterial material, EquipmentSlot slot, Settings settings, Item clientItem) {
        super(material, slot, settings);
        this.id = id;
        this.clientItem = clientItem;
        this.modelId = ServerOnlyItem.registerModelId(this);
        ServerOnlyItem.registerServerOnlyItem(id, this);
    }

    @Override
    public Set<ResourceIdentifier> getResources() {
        Set<ResourceIdentifier> resources = ServerOnlyItem.super.getResources();
        resources.add(new ResourceIdentifier("minecraft", "optifine/cit/armor/" + this.id.getPath() + ".properties", ResourceIdentifier.ResourceType.GENERIC_FILE));
        resources.add(new ResourceIdentifier("minecraft", "optifine/cit/armor/" + this.getMaterial().getName() + "_layer_1.png", ResourceIdentifier.ResourceType.GENERIC_FILE));
        resources.add(new ResourceIdentifier("minecraft", "optifine/cit/armor/" + this.getMaterial().getName() + "_layer_2.png", ResourceIdentifier.ResourceType.GENERIC_FILE));
        return resources;
    }

    @Override
    public Identifier getTextureId() {
        return null;
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
        return Optional.empty();
    }

    public Item asItem() {
        return this;
    }
}
