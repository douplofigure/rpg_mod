package douplo.item;

import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class WizardStaffItem extends CrossbowItem implements ServerOnlyItem {

    private final Identifier id;
    private final int modelId;

    public WizardStaffItem(Identifier id, Settings settings) {
        super(settings);
        this.id = id;
        this.modelId = ServerOnlyItem.registerModelId(this);
        ServerOnlyItem.registerServerOnlyItem(id, this);
    }

    public Predicate<ItemStack> getProjectiles() {
        return new Predicate<ItemStack>() {
            @Override
            public boolean test(ItemStack stack) {
                return stack.isOf(Items.FIRE_CHARGE);
            }
        };
    }

    @Override
    public Identifier getTextureId() {
        return null;
    }

    @Override
    public Set<ResourceIdentifier> getExtraResources() {
        Set<ResourceIdentifier> resources = new HashSet<>();
        resources.add(new ResourceIdentifier("rpg", "item/staff_crystal", ResourceIdentifier.ResourceType.TEXTURE));
        return resources;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public String getDisplayNameForLangFile() {
        return "Wizard Staff";
    }

    @Override
    public Item getClientItem() {
        return Items.CROSSBOW;
    }

    @Override
    public int getModelId() {
        return modelId;
    }

    @Override
    public Optional<Identifier> getCustomModelId() {
        return Optional.of(new Identifier("rpg", "item/wizard_staff"));
    }
}
