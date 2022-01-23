package douplo.item;


import com.google.gson.JsonObject;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.Set;

public class ServerArmorItem extends ArmorItem implements ServerOnlyItem {

    private final Identifier id;
    private final int modelId;

    private final ItemData itemData;

    public static final ServerOnlyItem.Serializer<ServerArmorItem> SERIALIZER = new ServerOnlyItem.Serializer<ServerArmorItem>() {
        @Override
        public ServerArmorItem fromJson(Identifier id, JsonObject json, ItemData data) {
            ArmorMaterial material = LoadedMaterial.getById(new Identifier(json.get("material").getAsString())).getArmorMaterial();
            EquipmentSlot slot = EquipmentSlot.byName(json.get("slot").getAsString());
            return new ServerArmorItem(id, material, slot, data.settings, data);
        }
    };

    public ServerArmorItem(Identifier id, ArmorMaterial material, EquipmentSlot slot, Settings settings, ItemData data) {
        super(material, slot, settings);
        this.id = id;
        this.itemData = data;
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
