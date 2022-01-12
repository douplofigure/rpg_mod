package douplo.loot.number;

import com.google.gson.*;
import douplo.RpgMod;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.util.JsonSerializer;

import java.util.ArrayList;
import java.util.List;

public class OperationNumberProvider implements LootNumberProvider {

    public static final JsonSerializer<OperationNumberProvider> SERIALIZER = new JsonSerializer<OperationNumberProvider>() {
        @Override
        public void toJson(JsonObject json, OperationNumberProvider object, JsonSerializationContext context) {

            String opName = object.op.toString().toLowerCase();
            json.addProperty("operation", opName);
            JsonArray array = new JsonArray();
            for (LootNumberProvider p : object.params) {
                JsonElement obj = context.serialize(p, LootNumberProvider.class);
                array.add(obj);
            }
            json.add("params", array);

        }

        @Override
        public OperationNumberProvider fromJson(JsonObject json, JsonDeserializationContext context) {

            String opName = json.get("operation").toString();
            Operation operation = Operation.fromString(opName);

            List<LootNumberProvider> params = new ArrayList<>();
            JsonArray array = json.getAsJsonArray("params");

            for (int i = 0; i < array.size(); ++i) {
                JsonElement elem = array.get(i);
                LootNumberProvider p = context.deserialize(elem, LootNumberProvider.class);
                params.add(p);
            }

            return new OperationNumberProvider(operation, params);
        }
    };

    public OperationNumberProvider(Operation operation, List<LootNumberProvider> params) {
        this.op = operation;
        this.params = params;
    }

    private static enum Operation {
        ADD,
        MUL,
        SUB,
        DIV,
        POW,
        ROOT,
        LOG,
        LOG_ADD_1;

        static Operation fromString(String string) {
            RpgMod.LOGGER.info(string.toUpperCase().substring(1, string.length()-1));
            return valueOf(string.toUpperCase().substring(1, string.length()-1));
        }
    }

    private List<LootNumberProvider> params;
    private Operation op;

    @Override
    public float nextFloat(LootContext context) {
        switch (op) {
            case ADD:
                return sum(params, context);

            case MUL:
                return prod(params, context);

            case DIV:
                return params.get(0).nextFloat(context) / params.get(1).nextFloat(context);

            case SUB:
                return params.get(0).nextFloat(context) - sum(params.subList(1, params.size()-1), context);

            case LOG:
                return (float) Math.log(params.get(0).nextFloat(context));

            case LOG_ADD_1:
                return (float) Math.log(params.get(0).nextFloat(context) + 1);

            case ROOT:
                return (float) Math.sqrt(params.get(0).nextFloat(context));

            case POW:
                return (float) Math.pow(params.get(0).nextFloat(context), params.get(1).nextFloat(context));

        }
        return 0;
    }

    private static float sum(List<LootNumberProvider> params, LootContext context) {
        float f = 0.0f;
        for (LootNumberProvider p : params) {
            f += p.nextFloat(context);
        }
        return f;
    }

    private static float prod(List<LootNumberProvider> params, LootContext context) {
        float f = 1.0f;
        for (LootNumberProvider p : params) {
            f *= p.nextFloat(context);
        }
        return f;
    }

    @Override
    public LootNumberProviderType getType() {
        return NumberProviderTypes.OPERATION;
    }
}
