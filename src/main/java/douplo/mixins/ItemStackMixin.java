package douplo.mixins;

import douplo.item.ServerOnlyItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

}
