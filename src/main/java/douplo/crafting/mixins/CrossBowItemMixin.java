package douplo.crafting.mixins;

import douplo.RpgMod;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(CrossbowItem.class)
public abstract class CrossBowItemMixin {

    private static final String CHARGED_PROJECTILES_KEY = "ChargedProjectiles";

    private static PersistentProjectileEntity createArrow(World world, LivingEntity entity, ItemStack crossbow, ItemStack arrow) {
        ArrowItem arrowItem = (ArrowItem)(arrow.getItem() instanceof ArrowItem ? arrow.getItem() : Items.ARROW);
        PersistentProjectileEntity persistentProjectileEntity = arrowItem.createArrow(world, arrow, entity);
        if (entity instanceof PlayerEntity) {
            persistentProjectileEntity.setCritical(true);
        }
        persistentProjectileEntity.setSound(SoundEvents.ITEM_CROSSBOW_HIT);
        persistentProjectileEntity.setShotFromCrossbow(true);
        int i = EnchantmentHelper.getLevel(Enchantments.PIERCING, crossbow);
        if (i > 0) {
            persistentProjectileEntity.setPierceLevel((byte)i);
        }
        return persistentProjectileEntity;
    }

    private static boolean isStaff(ItemStack crossbow) {

        NbtCompound tag = crossbow.getNbt();
        return tag.contains("CustomModelData") && tag.getInt("CustomModelData") == 1;

    }

    /**
     * @author
     * @reason
     * @return
     */
    @Overwrite
    public Predicate<ItemStack> getProjectiles() {
        return RangedWeaponItem.BOW_PROJECTILES.or(stack -> stack.isOf(Items.FIRE_CHARGE));
    }

    @Redirect(method = "shootAll",
    at=@At(value="INVOKE", target="Lnet/minecraft/item/CrossbowItem;shoot(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;FZFFF)V"))
    private static void shoot(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated) {
        ProjectileEntity projectileEntity;
        if (world.isClient) {
            return;
        }

        boolean bl = false;
        if (projectile.isOf(Items.FIRE_CHARGE) && isStaff(crossbow)) {
            Vec3d crossbowUser = shooter.getOppositeRotationVector(1.0f);
            Quaternion quaternion = new Quaternion(new Vec3f(crossbowUser), simulated, true);
            Vec3d vec3d = shooter.getRotationVec(1.0f);
            Vec3f vec3f = new Vec3f(vec3d);
            vec3f.rotate(quaternion);
            projectileEntity = new FireballEntity(world, shooter, vec3d.getX(), vec3d.getY(), vec3d.getZ(), 1);
            projectileEntity.setPosition(shooter.getX(), shooter.getEyeY() - 0.15, shooter.getZ());
            projectileEntity.setOwner(shooter);
            projectileEntity.setVelocity(vec3f.getX(), vec3f.getY(), vec3f.getZ(), speed, divergence);
        } else if (bl = projectile.isOf(Items.FIREWORK_ROCKET)) {
            projectileEntity = new FireworkRocketEntity(world, projectile, shooter, shooter.getX(), shooter.getEyeY() - (double)0.15f, shooter.getZ(), true);
        } else {
            projectileEntity = CrossBowItemMixin.createArrow(world, shooter, crossbow, projectile);
            if (creative || simulated != 0.0f) {
                ((PersistentProjectileEntity)projectileEntity).pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            }
        }
        if (shooter instanceof CrossbowUser) {
            CrossbowUser crossbowUser = (CrossbowUser)((Object)shooter);
            crossbowUser.shoot(crossbowUser.getTarget(), crossbow, projectileEntity, simulated);
        } else {
            Vec3d crossbowUser = shooter.getOppositeRotationVector(1.0f);
            Quaternion quaternion = new Quaternion(new Vec3f(crossbowUser), simulated, true);
            Vec3d vec3d = shooter.getRotationVec(1.0f);
            Vec3f vec3f = new Vec3f(vec3d);
            vec3f.rotate(quaternion);
            projectileEntity.setVelocity(vec3f.getX(), vec3f.getY(), vec3f.getZ(), speed, divergence);
        }
        crossbow.damage(bl ? 3 : 1, shooter, e -> e.sendToolBreakStatus(hand));
        world.spawnEntity(projectileEntity);
        world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0f, soundPitch);
    }

}

