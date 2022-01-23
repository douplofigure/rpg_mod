package douplo.item;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Predicate;

public class WizardStaffItem extends CrossbowItem implements ServerOnlyItem {

    private static final String CHARGED_KEY = "Charged";
    private static final String CHARGED_PROJECTILES_KEY = "ChargedProjectiles";

    private final Identifier id;
    private final int modelId;

    private final Set<Identifier> spells;

    public static final Serializer<WizardStaffItem> SERIALIZER = new Serializer<WizardStaffItem>() {
        @Override
        public WizardStaffItem fromJson(Identifier id, JsonObject json, ItemData data) {
            Set<Identifier> spells = new HashSet<>();
            JsonArray array = json.getAsJsonArray("spells");
            for (int i = 0; i < array.size(); ++i) {
                spells.add(new Identifier(array.get(i).getAsString()));
            }
            return new WizardStaffItem(id, data.settings, spells);
        }
    };

    public WizardStaffItem(Identifier id, Settings settings, Set<Identifier> spells) {
        super(settings);
        this.id = id;
        this.spells = spells;
        this.modelId = ServerOnlyItem.registerModelId(this);
        ServerOnlyItem.registerServerOnlyItem(id, this);
    }

    public Predicate<ItemStack> getProjectiles() {
        return new Predicate<ItemStack>() {
            @Override
            public boolean test(ItemStack stack) {

                if (stack.getItem() instanceof ServerOnlyItem) {
                    return spells.contains(((ServerOnlyItem) stack.getItem()).getId());
                } else {
                    return spells.contains(Registry.ITEM.getKey(stack.getItem()));
                }

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
        resources.add(new ResourceIdentifier("minecraft", "item/crossbow", ResourceIdentifier.ResourceType.MODEL));
        resources.add(new ResourceIdentifier("rpg", "item/wizard_staff", ResourceIdentifier.ResourceType.MODEL));
        resources.add(new ResourceIdentifier("rpg", "item/wizard_staff_charged", ResourceIdentifier.ResourceType.MODEL));
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
    public int getMaxDamageForClient() {
        return getMaxDamage();
    }

    @Override
    public Optional<Identifier> getCustomModelId() {
        return Optional.of(new Identifier("rpg", "item/wizard_staff"));
    }

    private static float getSpeed(ItemStack stack) {
        if (CrossbowItem.hasProjectile(stack, Items.FIREWORK_ROCKET)) {
            return 1.6f;
        }
        return 3.15f;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (CrossbowItem.isCharged(itemStack)) {
            WizardStaffItem.shootAll(world, user, hand, itemStack, getSpeed(itemStack), 1.0f);
            CrossbowItem.setCharged(itemStack, false);
            return TypedActionResult.consume(itemStack);
        }
        return super.use(world, user, hand);
    }

    private static List<ItemStack> getProjectiles(ItemStack crossbow) {
        NbtList nbtList;
        ArrayList<ItemStack> list = Lists.newArrayList();
        NbtCompound nbtCompound = crossbow.getNbt();
        if (nbtCompound != null && nbtCompound.contains(CHARGED_PROJECTILES_KEY, 9) && (nbtList = nbtCompound.getList(CHARGED_PROJECTILES_KEY, 10)) != null) {
            for (int i = 0; i < nbtList.size(); ++i) {
                NbtCompound nbtCompound2 = nbtList.getCompound(i);
                list.add(ItemStack.fromNbt(nbtCompound2));
            }
        }
        return list;
    }

    private static float[] getSoundPitches(Random random) {
        boolean bl = random.nextBoolean();
        return new float[]{1.0f, getSoundPitch(bl, random), getSoundPitch(!bl, random)};
    }

    private static float getSoundPitch(boolean flag, Random random) {
        float f = flag ? 0.63f : 0.43f;
        return 1.0f / (random.nextFloat() * 0.5f + 1.8f) + f;
    }


    private static void postShoot(World world, LivingEntity entity, ItemStack stack) {
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
            if (!world.isClient) {
                Criteria.SHOT_CROSSBOW.trigger(serverPlayerEntity, stack);
            }
            serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
        }
        clearProjectiles(stack);
    }

    private static void clearProjectiles(ItemStack crossbow) {
        NbtCompound nbtCompound = crossbow.getNbt();
        if (nbtCompound != null) {
            NbtList nbtList = nbtCompound.getList(CHARGED_PROJECTILES_KEY, 9);
            nbtList.clear();
            nbtCompound.put(CHARGED_PROJECTILES_KEY, nbtList);
        }
    }

    private static void shoot(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated) {
        ProjectileEntity projectileEntity;
        if (world.isClient) {
            return;
        }

        boolean bl = false;
        {
            Vec3d crossbowUser = shooter.getOppositeRotationVector(1.0f);
            Quaternion quaternion = new Quaternion(new Vec3f(crossbowUser), simulated, true);
            Vec3d vec3d = shooter.getRotationVec(1.0f);
            Vec3f vec3f = new Vec3f(vec3d);
            vec3f.rotate(quaternion);
            projectileEntity = new FireballEntity(world, shooter, vec3d.getX(), vec3d.getY(), vec3d.getZ(), 1);
            projectileEntity.setPosition(shooter.getX(), shooter.getEyeY() - 0.15, shooter.getZ());
            projectileEntity.setOwner(shooter);
            projectileEntity.setVelocity(vec3f.getX(), vec3f.getY(), vec3f.getZ(), speed, divergence);
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

    public static void shootAll(World world, LivingEntity entity, Hand hand, ItemStack stack, float speed, float divergence) {
        List<ItemStack> list = WizardStaffItem.getProjectiles(stack);
        float[] fs = getSoundPitches(entity.getRandom());
        for (int i = 0; i < list.size(); ++i) {
            boolean bl;
            ItemStack itemStack = list.get(i);
            boolean bl2 = bl = entity instanceof PlayerEntity && ((PlayerEntity)entity).getAbilities().creativeMode;
            if (itemStack.isEmpty()) continue;
            if (i == 0) {
                shoot(world, entity, hand, stack, itemStack, fs[i], bl, speed, divergence, 0.0f);
                continue;
            }
            if (i == 1) {
                shoot(world, entity, hand, stack, itemStack, fs[i], bl, speed, divergence, -10.0f);
                continue;
            }
            if (i != 2) continue;
            shoot(world, entity, hand, stack, itemStack, fs[i], bl, speed, divergence, 10.0f);
        }
        postShoot(world, entity, stack);
    }

}
