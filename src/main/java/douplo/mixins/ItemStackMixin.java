package douplo.mixins;

import douplo.item.ServerOnlyItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Redirect(method = "writeNbt", at=@At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;"))
    private <T> Identifier getId(DefaultedRegistry instance, T item) {
        if (item instanceof ServerOnlyItem) {
            return ((ServerOnlyItem) item).getId();
        }
        return instance.getId(item);
    }

    @Redirect(method = "isOf", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
    public Item getItemForComparison(ItemStack instance) {
        Item item = instance.getItem();
        if (item instanceof ServerOnlyItem) {
            item = Registry.ITEM.get(((ServerOnlyItem)item).getId());
        }
        return item;
    }

    @Redirect(method = "areItemsEqualIgnoreDamage", at=@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemEqualIgnoreDamage(Lnet/minecraft/item/ItemStack;)Z"))
    private static boolean compareItemsIgnoreDamage(ItemStack instance, ItemStack other) {
        Item instanceItem = instance.getItem();
        if (instanceItem instanceof ServerOnlyItem) {
            instanceItem = Registry.ITEM.get(((ServerOnlyItem) instanceItem).getId());
        }

        Item otherItem = instance.getItem();
        if (otherItem instanceof ServerOnlyItem) {
            otherItem = Registry.ITEM.get(((ServerOnlyItem) otherItem).getId());
        }

        return !instance.isEmpty() && instanceItem == otherItem;
    }

}
