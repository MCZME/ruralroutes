package github.mczme.ruralroutes.data.builder;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.theme.CompositionStrategy;
import github.mczme.ruralroutes.core.theme.CurrencyBasketEntry;
import github.mczme.ruralroutes.core.theme.FixedTradeEntry;
import github.mczme.ruralroutes.core.theme.InputEntry;
import github.mczme.ruralroutes.core.theme.ItemReference;
import github.mczme.ruralroutes.core.theme.OutputEntry;
import github.mczme.ruralroutes.core.theme.TradeContractEntry;
import github.mczme.ruralroutes.core.theme.TradeProfile;
import github.mczme.ruralroutes.core.trade.TradeSide;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * TradeProfile 构建器。
 * 保留与旧 ThemeBuilder 接近的写法，方便把内置主题交易内容迁到 profile。
 */
public class TradeProfileBuilder {

    private final String name;
    private final List<ItemReference> sellItems = new ArrayList<>();
    private final List<ItemReference> buyItems = new ArrayList<>();
    private final List<ItemReference> specialties = new ArrayList<>();
    private final List<TradeContractEntry> tradeContracts = new ArrayList<>();
    private boolean withCurrency = false;
    private BiConsumer<ResourceLocation, TradeProfile> registrar;

    private TradeProfileBuilder(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public static TradeProfileBuilder create(String name) {
        return new TradeProfileBuilder(name);
    }

    public TradeProfileBuilder withCurrency() {
        this.withCurrency = true;
        return this;
    }

    public TradeProfileBuilder registrar(BiConsumer<ResourceLocation, TradeProfile> registrar) {
        this.registrar = registrar;
        return this;
    }

    public TradeProfileBuilder sell(String... items) {
        for (String item : items) {
            sellItems.add(parseItemRef(item));
        }
        return this;
    }

    public TradeProfileBuilder sell(String item, Map<String, String> components) {
        sellItems.add(ItemReference.single(item, components));
        return this;
    }

    public TradeProfileBuilder sellPick(String item, int pick) {
        sellItems.add(parseItemRef(item, pick));
        return this;
    }

    public TradeProfileBuilder sellPick(int pick, String... items) {
        sellItems.add(parseItemGroup(null, pick, items));
        return this;
    }

    public TradeProfileBuilder sellPick(String key, int pick, String... items) {
        sellItems.add(parseItemGroup(key, pick, items));
        return this;
    }

    public TradeProfileBuilder buy(String... items) {
        for (String item : items) {
            buyItems.add(parseItemRef(item));
        }
        return this;
    }

    public TradeProfileBuilder buy(String item, Map<String, String> components) {
        buyItems.add(ItemReference.single(item, components));
        return this;
    }

    public TradeProfileBuilder buyPick(String item, int pick) {
        buyItems.add(parseItemRef(item, pick));
        return this;
    }

    public TradeProfileBuilder buyPick(int pick, String... items) {
        buyItems.add(parseItemGroup(null, pick, items));
        return this;
    }

    public TradeProfileBuilder buyPick(String key, int pick, String... items) {
        buyItems.add(parseItemGroup(key, pick, items));
        return this;
    }

    public TradeProfileBuilder specialty(String... items) {
        for (String item : items) {
            specialties.add(ItemReference.single(item));
        }
        return this;
    }

    public TradeProfileBuilder specialty(String item, Map<String, String> components) {
        specialties.add(ItemReference.single(item, components));
        return this;
    }

    public TradeProfileBuilder addFixedTrade(List<InputEntry> inputs, List<OutputEntry> outputs) {
        tradeContracts.add(new FixedTradeEntry(inputs, outputs));
        return this;
    }

    public TradeProfileBuilder addCurrencyBasketTrade(
        TradeSide side,
        List<String> items,
        List<String> acceptedCurrencies,
        CompositionStrategy composition
    ) {
        tradeContracts.add(new CurrencyBasketEntry(side, items, acceptedCurrencies, composition));
        return this;
    }

    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, name);
    }

    public TradeProfile build() {
        List<ItemReference> finalSellItems = new ArrayList<>(sellItems);
        List<ItemReference> finalBuyItems = new ArrayList<>(buyItems);

        if (withCurrency) {
            finalSellItems.add(ItemReference.single("#ruralroutes:currency"));
            finalBuyItems.add(ItemReference.single("#ruralroutes:currency"));
        }

        return new TradeProfile(
            getId(),
            List.copyOf(finalSellItems),
            List.copyOf(finalBuyItems),
            specialties.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(specialties)),
            Optional.empty(),
            tradeContracts.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(tradeContracts))
        );
    }

    public void register() {
        if (registrar != null) {
            registrar.accept(getId(), build());
        }
    }

    private static ItemReference parseItemRef(String str) {
        return parseItemRef(str, null);
    }

    private static ItemReference parseItemRef(String str, Integer pick) {
        return ItemReference.single(str, pick);
    }

    private static ItemReference parseItemGroup(String key, int pick, String... items) {
        List<String> refs = new ArrayList<>(items.length);
        for (String item : items) {
            refs.add(item);
        }
        return ItemReference.group(refs, pick, key);
    }
}
