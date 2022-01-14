package douplo.mixins;

import douplo.RpgMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.tag.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MiningToolItem.class)
public class MiningToolItemMixin {

    @Shadow
    private final Tag<Block> effectiveBlocks;
    @Shadow
    protected final float miningSpeed;

    public MiningToolItemMixin(Tag<Block> effectiveBlocks, float miningSpeed) {
        this.effectiveBlocks = effectiveBlocks;
        this.miningSpeed = miningSpeed;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        boolean isEffective = this.effectiveBlocks.contains(state.getBlock());
        RpgMod.LOGGER.info("Checking for mining speed " + isEffective);
        if (isEffective && stack.getNbt().contains("miningSpeed")){
            RpgMod.LOGGER.info("mining speed: " + stack.getNbt().getDouble("miningSpeed"));
            return (float) stack.getNbt().getDouble("miningSpeed");
        }
        return isEffective ? this.miningSpeed : 1.0f;
    }

}
