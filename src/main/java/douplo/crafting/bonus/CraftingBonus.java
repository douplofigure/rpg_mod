package douplo.crafting.bonus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import douplo.RpgMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootGsons;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CraftingBonus {

    protected static LootContextType CONTEXT_TYPE = new LootContextType.Builder().require(LootContextParameters.THIS_ENTITY).require(LootContextParameters.ORIGIN).build();

    public static Type EMPTY_TYPE = new Type(new Identifier(RpgMod.MODID, "empty"), new Loader() {
        @Override
        public CraftingBonus fromJSON(JsonObject object) {
            return EMPTY;
        }

    });

    public static final Type MODIFIER = new Type(new Identifier(RpgMod.MODID, "item_modifier"), new Loader() {
        @Override
        public CraftingBonus fromJSON(JsonObject object) {
            String modifierName = object.get("modifier").getAsString();
            return new ItemModifierBonus(new Identifier(modifierName));
        }
    });

    public static final Type EXPLOSION = new Type(new Identifier(RpgMod.MODID, "explosion"), new Loader() {
        @Override
        public CraftingBonus fromJSON(JsonObject object) {
            Identifier predicateId = new Identifier(object.get("predicate").getAsString());
            float power = object.get("power").getAsFloat();
            boolean fire = object.get("fire").getAsBoolean();
            return new ExplosionCraftBonus(predicateId, power, fire);
        }
    });

    public static final Type CHANGE_ITEM = new Type(new Identifier(RpgMod.MODID, "change_item"), new Loader() {
        @Override
        public CraftingBonus fromJSON(JsonObject object) {
            Identifier itemId = new Identifier(object.get("item").getAsString());
            return new ChangeItemCraftinBonus(Registry.ITEM.get(itemId));
        }
    });

    public static CraftingBonus EMPTY = new CraftingBonus(EMPTY_TYPE) {
        @Override
        public ItemStack applyResult(PlayerEntity entity, ItemStack stack, LootContext lootContext) {
            return stack;
        }
    };

    protected interface Loader {

        CraftingBonus fromJSON(JsonObject object);

    }

    public static class Type {

        private final Identifier id;
        private final Loader loader;

        private static Map<Identifier, Type> types = new HashMap<>();

        Type(Identifier id, Loader loader) {
            this.id = id;
            this.loader = loader;
            types.put(id, this);
        }

        public String toString() {
            return id.toString();
        }

        public static Type valueOf(String string) {
            Type t = types.get(new Identifier(string));
            RpgMod.LOGGER.info(t);
            return t;
        }

    }

    protected final Type type;
    protected List<LootCondition> conditions = new ArrayList<>();

    public CraftingBonus(Type type) {
        this.type = type;
    }

    private boolean conditionsOk(LootContext context) {
        RpgMod.LOGGER.info("Checking conditions for crafting bonus");
        for (LootCondition c : conditions) {
            RpgMod.LOGGER.info(c.toString());
            RpgMod.LOGGER.info(c.test(context));
            if (!c.test(context))
                return false;
        }
        return true;
    }

    public ItemStack apply(PlayerEntity entity, ItemStack stack) {

        LootContext.Builder builder = new LootContext.Builder((ServerWorld) entity.getWorld()).parameter(LootContextParameters.ORIGIN, entity.getPos()).parameter(LootContextParameters.THIS_ENTITY, entity);
        LootContext context = builder.build(CONTEXT_TYPE);

        if (conditionsOk(context)) {
            return applyResult(entity, stack, context);
        }
        return stack;

    }

    protected abstract ItemStack applyResult(PlayerEntity entity, ItemStack stack, LootContext lootContext);

    public static CraftingBonus fromJSON(JsonObject object) {

        RpgMod.LOGGER.info(object);
        String typeName = object.get("type").getAsString();
        Type type = Type.valueOf(typeName);
        CraftingBonus bonus = type.loader.fromJSON(object);

        GsonBuilder builder = LootGsons.getConditionGsonBuilder();
        Gson gson = builder.create();

        LootCondition[] conditions = gson.fromJson(object.get("conditions"), LootCondition[].class);
        if (conditions != null) {
            List<LootCondition> cond = List.of(conditions);
            bonus.setConditions(cond);
        }

        return bonus;

    }

    private void setConditions(List<LootCondition> cond) {
        this.conditions = cond;
    }

}
