package douplo.mixins;

import douplo.RpgMod;
import douplo.item.GenericServerItem;
import douplo.item.ServerOnlyItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PacketByteBuf.class)
public class PacketByteBufMixin {

    @Redirect(method = "writeItemStack",
    at=@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
    public Item getItemStackItem(ItemStack self) {
        Item item = self.getItem();
        if (item instanceof ServerOnlyItem){
            return ((ServerOnlyItem)item).getClientItem();
        }
        return item;
    }

    @Redirect(method = "writeItemStack",
    at=@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getNbt()Lnet/minecraft/nbt/NbtCompound;"))
    public NbtCompound getItemStackTagCompound(ItemStack self) {
        if (self.getItem() instanceof ServerOnlyItem) {
            return ((ServerOnlyItem)self.getItem()).getEncodedClientData(self);
        }
        return self.getNbt();
    }

    @Inject(method = "readItemStack", at=@At(value = "RETURN"), cancellable = true)
    public void postProcessItemStack(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();
        NbtCompound tag = stack.getNbt();
        if (tag != null && tag.contains("ServerOnlyItem") && tag.getByte("ServerOnlyItem") != 0) {
            int modelId = tag.getInt("CustomModelData");
            ServerOnlyItem item = ServerOnlyItem.getFromItemAndModel(stack.getItem(), modelId);
            ItemStack realStack = new ItemStack(item, stack.getCount());
            realStack.setDamage(stack.getDamage());
            realStack.setNbt(tag.copy());
            cir.setReturnValue(realStack);
        }

    }

}
