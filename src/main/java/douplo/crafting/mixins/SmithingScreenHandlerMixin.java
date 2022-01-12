package douplo.crafting.mixins;

import douplo.crafting.bonus.BonusRecipe;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(SmithingScreenHandler.class)
public abstract class SmithingScreenHandlerMixin extends ForgingScreenHandler {

    @Shadow @Final private World world;
    @Shadow private SmithingRecipe currentRecipe;

    public SmithingScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void updateResult() {
        List<SmithingRecipe> list = this.world.getRecipeManager().getAllMatches(RecipeType.SMITHING, this.input, this.world);
        if (list.isEmpty()) {
            this.output.setStack(0, ItemStack.EMPTY);
        } else {
            this.currentRecipe = list.get(0);
            ItemStack itemStack;
            if (this.currentRecipe instanceof BonusRecipe && !player.getWorld().isClient()) {
                itemStack = ((BonusRecipe) this.currentRecipe).craft(this.input, player);
            } else {
                itemStack = this.currentRecipe.craft(this.input);
            }
            this.output.setLastRecipe(this.currentRecipe);
            this.output.setStack(0, itemStack);
        }
    }

}
