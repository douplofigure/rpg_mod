package douplo.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.Identifier;

public class ServerToolItem extends ServerOnlyItem {

    private final ToolMaterial material;

    public ServerToolItem(Identifier id, ToolMaterial material, Item.Settings settings) {
        super(id, settings.maxDamageIfAbsent(material.getDurability()));
        this.material = material;
    }

    public ToolMaterial getMaterial() {
        return this.material;
    }

    @Override
    public int getEnchantability() {
        return this.material.getEnchantability();
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return this.material.getRepairIngredient().test(ingredient) || super.canRepair(stack, ingredient);
    }

    protected JsonObject getModelData() {
        JsonObject modelData = new JsonObject();
        modelData.addProperty("parent", "item/handheld");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", this.getId().getNamespace() + ":item/" + this.getId().getPath());
        modelData.add("textures", textures);
        return modelData;
    }
}
