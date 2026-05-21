package github.mczme.ruralroutes.data.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.trade.TradeTargetRef;
import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import github.mczme.ruralroutes.core.trade.TradeSide;
import net.minecraft.resources.ResourceLocation;

/**
 * 主题模板构建器
 * 简化 ThemeDataProvider 中的主题定义
 */
public class ThemeBuilder {

    private final String name;
    private String biome;
    private final List<ThemeTemplate.ItemReference> sellItems = new ArrayList<>();
    private final List<ThemeTemplate.ItemReference> buyItems = new ArrayList<>();
    private final List<ThemeTemplate.ItemReference> specialties = new ArrayList<>();
    private ThemeTemplate.StockRange defaultStock;
    private final Map<String, ThemeTemplate.StockTarget> stockTargets = new LinkedHashMap<>();
    private final Map<String, ThemeTemplate.StockRange> stockSpecific = new LinkedHashMap<>();
    private final List<ThemeTemplate.PriceModifier> priceModifiers = new ArrayList<>();
    private final List<ThemeTemplate.TradeContractEntry> tradeContracts = new ArrayList<>();
    private boolean withCurrency = false;

    private BiConsumer<ResourceLocation, ThemeTemplate> registrar;

    private ThemeBuilder(String name) {
        this.name = name;
    }

    public static ThemeBuilder create(String name) {
        return new ThemeBuilder(name);
    }

    /**
     * 添加默认货币到出售池和收购池
     * 村庄会出售和收购铜板（基础货币）
     */
    public ThemeBuilder withCurrency() {
        this.withCurrency = true;
        return this;
    }

    /**
     * 设置注册器（由 ThemeDataProvider 调用）
     */
    public ThemeBuilder registrar(BiConsumer<ResourceLocation, ThemeTemplate> registrar) {
        this.registrar = registrar;
        return this;
    }

    public ThemeBuilder biome(String biome) {
        this.biome = biome;
        return this;
    }

    /**
     * 添加出售物品
     * 格式: "tag:xxx" 表示标签，其他为物品ID
     */
    public ThemeBuilder sell(String... items) {
        for (String item : items) {
            sellItems.add(parseItemRef(item));
        }
        return this;
    }

    /**
     * 添加带组件的出售物品。
     */
    public ThemeBuilder sell(String item, Map<String, String> components) {
        sellItems.add(ThemeTemplate.ItemReference.single(normalizeRef(item), components));
        return this;
    }

    /**
     * 添加出售候选，并在展开后随机抽取指定数量。
     * 适合标签引用；精确物品引用即使设置 pick 也只会得到单个物品。
     */
    public ThemeBuilder sellPick(String item, int pick) {
        sellItems.add(parseItemRef(item, pick));
        return this;
    }

    /**
     * 添加出售候选组，并在展开后随机抽取指定数量。
     * 候选组可混用标签与精确物品。
     */
    public ThemeBuilder sellPick(int pick, String... items) {
        sellItems.add(parseItemGroup(null, pick, items));
        return this;
    }

    /**
     * 添加带稳定来源键的出售候选组，并在展开后随机抽取指定数量。
     */
    public ThemeBuilder sellPick(String key, int pick, String... items) {
        sellItems.add(parseItemGroup(key, pick, items));
        return this;
    }

    /**
     * 添加收购物品
     */
    public ThemeBuilder buy(String... items) {
        for (String item : items) {
            buyItems.add(parseItemRef(item));
        }
        return this;
    }

    /**
     * 添加带组件的收购物品。
     */
    public ThemeBuilder buy(String item, Map<String, String> components) {
        buyItems.add(ThemeTemplate.ItemReference.single(normalizeRef(item), components));
        return this;
    }

    /**
     * 添加收购候选，并在展开后随机抽取指定数量。
     */
    public ThemeBuilder buyPick(String item, int pick) {
        buyItems.add(parseItemRef(item, pick));
        return this;
    }

    /**
     * 添加收购候选组，并在展开后随机抽取指定数量。
     * 候选组可混用标签与精确物品。
     */
    public ThemeBuilder buyPick(int pick, String... items) {
        buyItems.add(parseItemGroup(null, pick, items));
        return this;
    }

    /**
     * 添加带稳定来源键的收购候选组，并在展开后随机抽取指定数量。
     */
    public ThemeBuilder buyPick(String key, int pick, String... items) {
        buyItems.add(parseItemGroup(key, pick, items));
        return this;
    }

    /**
     * 添加主题特产
     */
    public ThemeBuilder specialty(String... items) {
        for (String item : items) {
            specialties.add(ThemeTemplate.ItemReference.single(normalizeRef(item)));
        }
        return this;
    }

    /**
     * 添加带组件的主题特产。
     */
    public ThemeBuilder specialty(String item, Map<String, String> components) {
        specialties.add(ThemeTemplate.ItemReference.single(normalizeRef(item), components));
        return this;
    }

    /**
     * 设置默认库存范围
     */
    public ThemeBuilder stock(int min, int max) {
        this.defaultStock = new ThemeTemplate.StockRange(min, max);
        return this;
    }

    /**
     * 添加特定物品库存配置
     */
    public ThemeBuilder stockSpecific(String key, int min, int max) {
        stockSpecific.put(key, new ThemeTemplate.StockRange(min, max));
        return this;
    }

    /**
     * 设置目标库存的共享范围。
     */
    public ThemeBuilder stockTarget(String key, int min, int max) {
        stockTargets.put(key, ThemeTemplate.StockTarget.shared(new ThemeTemplate.StockRange(min, max)));
        return this;
    }

    /**
     * 设置目标库存的分向范围。
     */
    public ThemeBuilder stockTarget(String key, int sellMin, int sellMax, int buyMin, int buyMax) {
        stockTargets.put(key, ThemeTemplate.StockTarget.directional(
            new ThemeTemplate.StockRange(sellMin, sellMax),
            new ThemeTemplate.StockRange(buyMin, buyMax)
        ));
        return this;
    }

    /**
     * 添加价格修正
     */
    public ThemeBuilder priceModifier(String key, float sell, float buy) {
        priceModifiers.add(ThemeTemplate.PriceModifier.of(TradeTargetRef.fromString(key), sell, buy));
        return this;
    }

    /**
     * 添加固定交换契约
     */
    public ThemeBuilder addFixedTrade(List<ThemeTemplate.InputEntry> inputs, List<ThemeTemplate.OutputEntry> outputs) {
        tradeContracts.add(new ThemeTemplate.FixedTradeEntry(inputs, outputs));
        return this;
    }

    /**
     * 添加动态货币篮契约
     */
    public ThemeBuilder addCurrencyBasketTrade(
            TradeSide side,
            List<String> items,
            List<String> acceptedCurrencies,
            ThemeTemplate.CompositionStrategy composition) {
        tradeContracts.add(new ThemeTemplate.CurrencyBasketEntry(side, items, acceptedCurrencies, composition));
        return this;
    }

    /**
     * 获取资源位置
     */
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, name);
    }

    /**
     * 构建 ThemeTemplate
     */
    public ThemeTemplate build() {
        List<ThemeTemplate.ItemReference> finalSellItems = new ArrayList<>(sellItems);
        List<ThemeTemplate.ItemReference> finalBuyItems = new ArrayList<>(buyItems);

        // 添加默认货币到出售池和收购池
        if (withCurrency) {
            finalSellItems.add(ThemeTemplate.ItemReference.single("#ruralroutes:currency"));
            finalBuyItems.add(ThemeTemplate.ItemReference.single("#ruralroutes:currency"));
        }

        return new ThemeTemplate(
            getId(),
            ResourceLocation.parse(biome),
            List.copyOf(finalSellItems),
            List.copyOf(finalBuyItems),
            specialties.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(specialties)),
            defaultStock == null && stockTargets.isEmpty() && stockSpecific.isEmpty()
                ? Optional.empty()
                : Optional.of(new ThemeTemplate.StockConfig(
                    Optional.ofNullable(defaultStock),
                    stockTargets.isEmpty() ? Optional.empty()
                        : Optional.of(Collections.unmodifiableMap(new LinkedHashMap<>(stockTargets))),
                    stockSpecific.isEmpty() ? Optional.empty()
                        : Optional.of(Collections.unmodifiableMap(new LinkedHashMap<>(stockSpecific)))
                )),
            priceModifiers.isEmpty() ? Optional.empty()
                : Optional.of(List.copyOf(priceModifiers)),
            tradeContracts.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(tradeContracts))
        );
    }

    /**
     * 构建并注册主题
     */
    public void register() {
        if (registrar != null) {
            registrar.accept(getId(), build());
        }
    }

    /**
     * 解析物品引用字符串
     * "tag:xxx" -> "#xxx"（标签）
     * "xxx" -> "xxx"（物品）
     */
    private static ThemeTemplate.ItemReference parseItemRef(String str) {
        return parseItemRef(str, null);
    }

    private static ThemeTemplate.ItemReference parseItemRef(String str, Integer pick) {
        return ThemeTemplate.ItemReference.single(normalizeRef(str), pick);
    }

    private static ThemeTemplate.ItemReference parseItemGroup(String key, int pick, String... items) {
        List<String> refs = new ArrayList<>(items.length);
        for (String item : items) {
            refs.add(normalizeRef(item));
        }
        return ThemeTemplate.ItemReference.group(refs, pick, key);
    }

    private static String normalizeRef(String str) {
        if (str.startsWith("tag:")) {
            String tagId = str.substring(4);
            return tagId.startsWith("#") ? tagId : "#" + tagId;
        }
        return str;
    }

}
