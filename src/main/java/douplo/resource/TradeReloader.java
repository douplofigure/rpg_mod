package douplo.resource;

import com.google.gson.JsonObject;
import douplo.RpgMod;
import douplo.crafting.bonus.BonusRecipe;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TradeReloader implements Reloader {

    public static final String TRADE_DIRECTORY = "trading";
    public static final String WANDERING_TRADER_DIRECTORY = TRADE_DIRECTORY + "/wandering_trader";

    private static final int WANDERING_TRADE_1_ENTRIES = 62;
    private static final int WANDERING_TRADE_2_ENTRIES = 6;

    static class CurrencyTradeFactory implements TradeOffers.Factory {

        private final TradeOffers.Factory factory;
        private final Item currencyItem;

        CurrencyTradeFactory(TradeOffers.Factory factory, Item currencyItem) {
            this.factory = factory;
            this.currencyItem = currencyItem;
        }

        private ItemStack replaceWithCurrency(ItemStack stack) {
            if (stack.isOf(Items.EMERALD)) {
                return new ItemStack(currencyItem, stack.getCount());
            }
            return stack;
        }

        @Nullable
        @Override
        public TradeOffer create(Entity entity, Random random) {
            TradeOffer offer = factory.create(entity, random);
            ItemStack buy1 = replaceWithCurrency(offer.getOriginalFirstBuyItem());
            ItemStack buy2 = replaceWithCurrency(offer.getSecondBuyItem());
            ItemStack sell = replaceWithCurrency(offer.getSellItem());
            TradeOffer result = new TradeOffer(buy1, buy2, sell, offer.getMaxUses(), offer.getMerchantExperience(), offer.getPriceMultiplier());
            return result;
        }
    }

    /**
     * Swap currency of all existing trades
     * @param coinId
     */
    private static void applyNewCurrency(Identifier coinId) {

        Item currencyItem = Registry.ITEM.get(coinId);

        for (Map.Entry<Integer, TradeOffers.Factory[]> entry : TradeOffers.WANDERING_TRADER_TRADES.int2ObjectEntrySet()) {
            TradeOffers.Factory[] factories = entry.getValue();
            for (int i = 0; i < factories.length; ++i) {
                factories[i] = new CurrencyTradeFactory(factories[i], currencyItem);
            }

            RpgMod.LOGGER.info("Trades " + entry.getKey() + " has " + factories.length + " entries");

        }

        for (Map.Entry<VillagerProfession, Int2ObjectMap<TradeOffers.Factory[]>> villagerEntry : TradeOffers.PROFESSION_TO_LEVELED_TRADE.entrySet()) {

            for (Map.Entry<Integer, TradeOffers.Factory[]> entry : villagerEntry.getValue().int2ObjectEntrySet()) {
                TradeOffers.Factory[] factories = entry.getValue();
                for (int i = 0; i < factories.length; ++i) {
                    factories[i] = new CurrencyTradeFactory(factories[i], currencyItem);
                }

            }

        }

    }

    private static class CustomTradeOffer extends TradeOffer {

        public CustomTradeOffer(ItemStack firstBuyItem, ItemStack secondBuyItem, ItemStack sellItem, int maxUses, int merchantExperience, float priceMultiplier) {
            super(firstBuyItem, secondBuyItem, sellItem, maxUses, merchantExperience, priceMultiplier);
        }


    }

    @Override
    public void reload(ResourceManager manager) {

        for (Identifier filename : manager.findResources(TRADE_DIRECTORY, path -> path.endsWith("currency.json"))) {

            try {
                JsonObject data = ReloadUtils.jsonFromStream(manager.getResource(filename).getInputStream());
                Identifier coinId = new Identifier(data.get("item").getAsString());
                applyNewCurrency(coinId);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        List<TradeOffers.Factory> loadedFactories = new ArrayList<>();
        for (Identifier filename : manager.findResources(WANDERING_TRADER_DIRECTORY, path -> path.endsWith(".json"))) {

            try {
                JsonObject data = ReloadUtils.jsonFromStream(manager.getResource(filename).getInputStream());
                loadedFactories.add(loadTradeFactory(data));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        TradeOffers.Factory[] factories = new TradeOffers.Factory[WANDERING_TRADE_1_ENTRIES + loadedFactories.size()];
        for (int i = 0; i < WANDERING_TRADE_1_ENTRIES; ++i) {
            factories[i] = TradeOffers.WANDERING_TRADER_TRADES.get(1)[i];
        }
        for (int i = 0; i < loadedFactories.size(); ++i) {
            factories[i+WANDERING_TRADE_1_ENTRIES] = loadedFactories.get(i);
        }
        TradeOffers.WANDERING_TRADER_TRADES.put(1, factories);

    }

    static class LoadedFactory implements TradeOffers.Factory {

        private final ItemStack buy1;
        private final ItemStack buy2;
        private final ItemStack sell;

        public LoadedFactory(ItemStack buy1, ItemStack buy2, ItemStack sell) {
            this.buy1 = buy1;
            this.buy2 = buy2;
            this.sell = sell;
        }

        @Nullable
        @Override
        public TradeOffer create(Entity entity, Random random) {
            return new TradeOffer(buy1, buy2, sell, 12, 1, 1.0f);
        }
    }

    private TradeOffers.Factory loadTradeFactory(JsonObject data) {

        ItemStack buy1 = BonusRecipe.readResultStack(data.get("buy").getAsJsonObject());
        ItemStack buy2 = ItemStack.EMPTY;
        if (data.has("buy_extra")) {
            buy2 = BonusRecipe.readResultStack(data.get("buy_extra").getAsJsonObject());
        }
        ItemStack sell = BonusRecipe.readResultStack(data.get("sell").getAsJsonObject());

        return new LoadedFactory(buy1, buy2, sell);

    }
}
