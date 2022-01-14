package douplo.item;

import com.google.common.collect.Multimap;
import douplo.RpgMod;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.HashMap;
import java.util.Map;

public class ServerOnlyItem extends Item {

    private final Item clientItem;
    private final int modelId;
    private static Map<Item, Integer> modelIds = new HashMap<>();

    public ServerOnlyItem(Settings settings) {
        super(settings);
        if (this.getMaxCount() == 1) {
            this.clientItem = Items.DIAMOND_HOE;
        } else if (this.getMaxCount() == 16) {
            this.clientItem = Items.ENDER_PEARL;
        } else {
            this.clientItem = Items.DIRT;
        }
        modelId = registerModelId(this);
    }

    public ServerOnlyItem(Settings settings, Item clientItem) {
        super(settings);
        this.clientItem = clientItem;
        modelId = registerModelId(this);
    }

    public Item getClientItem() {
        return this.clientItem;
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

        RpgMod.LOGGER.info("Sending tag: " + tag + " for item " + this);

        return tag;
    }

    private static int registerModelId(ServerOnlyItem serverOnlyItem) {

        Item clientItem = serverOnlyItem.getClientItem();
        if (!modelIds.containsKey(clientItem))
            modelIds.put(clientItem, 0);

        int modelId = modelIds.get(clientItem) + 1;
        modelIds.put(clientItem, modelId);
        return modelId;

    }


}
