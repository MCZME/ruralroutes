package github.mczme.ruralroutes.core.theme;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.core.trade.TradeSide;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 主题模板原始数据。
 * 主题可直接定义交易内容，也可引用一个或多个 TradeProfile 进行组合。
 */
public class ThemeTemplate {

    public static final Codec<ThemeTemplate> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("name").forGetter(ThemeTemplate::name),
            ResourceLocation.CODEC.fieldOf("biome").forGetter(ThemeTemplate::biome),
            ItemReference.CODEC.listOf().optionalFieldOf("sell_items", List.of()).forGetter(ThemeTemplate::sellItems),
            ItemReference.CODEC.listOf().optionalFieldOf("buy_items", List.of()).forGetter(ThemeTemplate::buyItems),
            ItemReference.CODEC.listOf().optionalFieldOf("theme_specialties").forGetter(ThemeTemplate::themeSpecialtyItems),
            StockConfig.CODEC.optionalFieldOf("stock").forGetter(ThemeTemplate::stock),
            Codec.unboundedMap(Codec.STRING, PriceModifier.CODEC).optionalFieldOf("price_modifiers").forGetter(ThemeTemplate::priceModifiers),
            TradeContractEntry.CODEC.listOf().optionalFieldOf("trade_contracts").forGetter(ThemeTemplate::tradeContracts),
            ResourceLocation.CODEC.listOf().optionalFieldOf("trade_profiles").forGetter(ThemeTemplate::tradeProfiles)
        ).apply(instance, ThemeTemplate::new)
    );

    private final ResourceLocation name;
    private final ResourceLocation biome;
    private final List<ItemReference> sellItems;
    private final List<ItemReference> buyItems;
    private final Optional<List<ItemReference>> themeSpecialtyItems;
    private final Optional<StockConfig> stock;
    private final Optional<Map<String, PriceModifier>> priceModifiers;
    private final Optional<List<TradeContractEntry>> tradeContracts;
    private final Optional<List<ResourceLocation>> tradeProfiles;

    public ThemeTemplate(
        ResourceLocation name,
        ResourceLocation biome,
        List<ItemReference> sellItems,
        List<ItemReference> buyItems,
        Optional<List<ItemReference>> themeSpecialtyItems,
        Optional<StockConfig> stock,
        Optional<Map<String, PriceModifier>> priceModifiers,
        Optional<List<TradeContractEntry>> tradeContracts
    ) {
        this(name, biome, sellItems, buyItems, themeSpecialtyItems, stock, priceModifiers, tradeContracts, Optional.empty());
    }

    public ThemeTemplate(
        ResourceLocation name,
        ResourceLocation biome,
        List<ItemReference> sellItems,
        List<ItemReference> buyItems,
        Optional<List<ItemReference>> themeSpecialtyItems,
        Optional<StockConfig> stock,
        Optional<Map<String, PriceModifier>> priceModifiers,
        Optional<List<TradeContractEntry>> tradeContracts,
        Optional<List<ResourceLocation>> tradeProfiles
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.biome = Objects.requireNonNull(biome, "biome");
        this.sellItems = List.copyOf(Objects.requireNonNull(sellItems, "sellItems"));
        this.buyItems = List.copyOf(Objects.requireNonNull(buyItems, "buyItems"));
        this.themeSpecialtyItems = themeSpecialtyItems.map(List::copyOf);
        this.stock = Objects.requireNonNull(stock, "stock");
        this.priceModifiers = priceModifiers.map(Map::copyOf);
        this.tradeContracts = tradeContracts.map(List::copyOf);
        this.tradeProfiles = tradeProfiles.map(List::copyOf);
    }

    public ResourceLocation name() {
        return name;
    }

    public ResourceLocation biome() {
        return biome;
    }

    public List<ItemReference> sellItems() {
        return sellItems;
    }

    public List<ItemReference> buyItems() {
        return buyItems;
    }

    public Optional<List<ItemReference>> themeSpecialtyItems() {
        return themeSpecialtyItems;
    }

    public Optional<List<ResourceLocation>> themeSpecialties() {
        return themeSpecialtyItems.map(items -> items.stream()
            .filter(ItemReference::isExactItem)
            .map(ItemReference::itemId)
            .map(ResourceLocation::parse)
            .toList());
    }

    public Optional<StockConfig> stock() {
        return stock;
    }

    public Optional<Map<String, PriceModifier>> priceModifiers() {
        return priceModifiers;
    }

    public Optional<List<TradeContractEntry>> tradeContracts() {
        return tradeContracts;
    }

    public Optional<List<ResourceLocation>> tradeProfiles() {
        return tradeProfiles;
    }

    /**
     * 物品引用，支持标签、精确物品，以及带组件的精确物品。
     * 可使用纯字符串、单引用对象，或混合候选组对象。
     */
    public record ItemReference(
        Optional<String> id,
        Optional<List<ItemEntry>> items,
        Optional<Integer> pick,
        Optional<String> key,
        Optional<Map<String, String>> components
    ) {
        public ItemReference {
            id = id.map(String::strip).filter(str -> !str.isEmpty());
            items = items.map(list -> List.copyOf(list.stream().map(Objects::requireNonNull).toList()));
            key = key.map(String::strip).filter(str -> !str.isEmpty());
            components = components.map(Map::copyOf);

            boolean hasId = id.isPresent();
            boolean hasItems = items.isPresent();
            if (hasId == hasItems) {
                throw new IllegalArgumentException("ItemReference must define exactly one of id or items");
            }
            if (items.isPresent() && items.get().isEmpty()) {
                throw new IllegalArgumentException("ItemReference items cannot be empty");
            }
            if (components.isPresent() && id.isEmpty()) {
                throw new IllegalArgumentException("ItemReference components require a single item id");
            }
        }

        private static final Codec<ItemEntry> ITEM_ENTRY_CODEC = Codec.either(Codec.STRING, ItemEntry.OBJECT_CODEC)
            .xmap(
                either -> either.map(ItemEntry::fromString, entry -> entry),
                entry -> entry.canUseStringShorthand()
                    ? Either.left(entry.ref())
                    : Either.right(entry)
            );

        private static final Codec<ItemReference> OBJECT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.STRING.optionalFieldOf("id").forGetter(ItemReference::id),
                ITEM_ENTRY_CODEC.listOf().optionalFieldOf("items").forGetter(ItemReference::items),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("pick").forGetter(ItemReference::pick),
                Codec.STRING.optionalFieldOf("key").forGetter(ItemReference::key),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("components").forGetter(ItemReference::components)
            ).apply(instance, ItemReference::new)
        );

        public static final Codec<ItemReference> CODEC = Codec.either(Codec.STRING, OBJECT_CODEC)
            .xmap(
                either -> either.map(ItemReference::single, ref -> ref),
                ref -> ref.canUseStringShorthand()
                    ? Either.left(ref.id().orElseThrow())
                    : Either.right(ref)
            );

        public static ItemReference single(String id) {
            return new ItemReference(Optional.of(id), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        public static ItemReference single(String id, Integer pick) {
            return new ItemReference(Optional.of(id), Optional.empty(), Optional.ofNullable(pick), Optional.empty(), Optional.empty());
        }

        public static ItemReference single(String id, Map<String, String> components) {
            return new ItemReference(Optional.of(id), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(components));
        }

        public static ItemReference single(String id, Integer pick, Map<String, String> components) {
            return new ItemReference(Optional.of(id), Optional.empty(), Optional.ofNullable(pick), Optional.empty(), Optional.of(components));
        }

        public static ItemReference group(List<String> refs, Integer pick, String key) {
            return new ItemReference(
                Optional.empty(),
                Optional.of(refs.stream().map(ItemEntry::fromString).toList()),
                Optional.ofNullable(pick),
                Optional.ofNullable(key),
                Optional.empty()
            );
        }

        public static ItemReference groupEntries(List<ItemEntry> refs, Integer pick, String key) {
            return new ItemReference(Optional.empty(), Optional.of(refs), Optional.ofNullable(pick), Optional.ofNullable(key), Optional.empty());
        }

        public boolean isSingle() {
            return id.isPresent();
        }

        public boolean isGroup() {
            return items.isPresent();
        }

        public List<String> refs() {
            return items.map(list -> list.stream().map(ItemEntry::ref).toList())
                .orElseGet(() -> List.of(id.orElseThrow()));
        }

        public List<ItemEntry> itemEntries() {
            return items.orElseGet(() -> List.of(new ItemEntry(Optional.of(id.orElseThrow()), components)));
        }

        public String sourceKey() {
            if (key.isPresent()) {
                return key.get();
            }
            if (id.isPresent()) {
                return id.get();
            }
            return "group:" + String.join("|", refs());
        }

        public String debugLabel() {
            return isSingle() ? id.orElse("<unknown>") : refs().toString();
        }

        public boolean canUseStringShorthand() {
            return id.isPresent() && items.isEmpty() && pick.isEmpty() && key.isEmpty() && components.isEmpty();
        }

        public boolean isTag() {
            return id.map(ref -> ref.startsWith("#")).orElse(false);
        }

        public String itemId() {
            String ref = id.orElseThrow(() -> new IllegalStateException("Grouped ItemReference has no single item id"));
            return isTag() ? ref.substring(1) : ref;
        }

        public boolean hasPickLimit() {
            return pick.isPresent();
        }

        public boolean isExactItem() {
            return id.isPresent() && !isTag();
        }
    }

    /**
     * 候选条目中的单个物品定义。
     * 保留字符串简写，必要时可携带组件信息。
     */
    public record ItemEntry(
        Optional<String> id,
        Optional<Map<String, String>> components
    ) {
        public ItemEntry {
            id = id.map(String::strip).filter(str -> !str.isEmpty());
            components = components.map(Map::copyOf);
            if (id.isEmpty()) {
                throw new IllegalArgumentException("ItemEntry requires an id");
            }
        }

        private static final Codec<ItemEntry> OBJECT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.STRING.optionalFieldOf("id").forGetter(ItemEntry::id),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("components").forGetter(ItemEntry::components)
            ).apply(instance, ItemEntry::new)
        );

        public static final Codec<ItemEntry> CODEC = Codec.either(Codec.STRING, OBJECT_CODEC)
            .xmap(
                either -> either.map(ItemEntry::fromString, entry -> entry),
                entry -> entry.canUseStringShorthand()
                    ? Either.left(entry.ref())
                    : Either.right(entry)
            );

        public static ItemEntry fromString(String id) {
            return new ItemEntry(Optional.of(id), Optional.empty());
        }

        public boolean canUseStringShorthand() {
            return components.isEmpty();
        }

        public boolean isTag() {
            return id.map(ref -> ref.startsWith("#")).orElse(false);
        }

        public String ref() {
            return id.orElseThrow();
        }
    }

    /**
     * 库存配置。
     * targets 支持共享范围或按 sell/buy 分向范围；specific 保留旧的精确覆盖映射。
     */
    public record StockConfig(
        Optional<StockRange> defaultRange,
        Optional<Map<String, StockTarget>> targetEntries,
        Optional<Map<String, StockRange>> specific
    ) {
        public static final Codec<StockConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                StockRange.CODEC.optionalFieldOf("default").forGetter(StockConfig::defaultRange),
                Codec.unboundedMap(Codec.STRING, StockTarget.CODEC).optionalFieldOf("targets").forGetter(StockConfig::targetEntries),
                Codec.unboundedMap(Codec.STRING, StockRange.CODEC).optionalFieldOf("specific").forGetter(StockConfig::specific)
            ).apply(instance, (defaultRange, targets, specific) -> new StockConfig(
                defaultRange,
                targets,
                specific.isPresent() ? specific : targets.map(StockConfig::projectSharedTargets)
            ))
        );

        public StockConfig {
            defaultRange = Objects.requireNonNull(defaultRange, "defaultRange");
            targetEntries = targetEntries.map(Map::copyOf);
            specific = specific.map(Map::copyOf);
        }

        public StockConfig(Optional<StockRange> defaultRange, Optional<Map<String, StockRange>> targets) {
            this(defaultRange, targets.map(StockConfig::toTargetEntries), targets);
        }

        public Optional<Map<String, StockRange>> targets() {
            return targetEntries.map(StockConfig::projectSharedTargets);
        }

        public Optional<Map<String, StockTarget>> targetEntries() {
            return targetEntries;
        }

        public Optional<Map<String, StockRange>> specific() {
            return specific;
        }

        private static Map<String, StockRange> projectSharedTargets(Map<String, StockTarget> targets) {
            return targets.entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().toLegacyRange()
                ));
        }

        private static Map<String, StockTarget> toTargetEntries(Map<String, StockRange> targets) {
            return targets.entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                    Map.Entry::getKey,
                    entry -> StockTarget.shared(entry.getValue())
                ));
        }
    }

    /**
     * 目标库存表达。
     * 可使用共享范围，或按出售/收购方向分别声明。
     */
    public record StockTarget(
        Optional<StockRange> shared,
        Optional<StockRange> sell,
        Optional<StockRange> buy
    ) {
        private static final Codec<StockTarget> OBJECT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                StockRange.CODEC.optionalFieldOf("shared").forGetter(StockTarget::shared),
                StockRange.CODEC.optionalFieldOf("sell").forGetter(StockTarget::sell),
                StockRange.CODEC.optionalFieldOf("buy").forGetter(StockTarget::buy)
            ).apply(instance, StockTarget::new)
        );

        public static final Codec<StockTarget> CODEC = Codec.either(StockRange.CODEC, OBJECT_CODEC)
            .xmap(
                either -> either.map(StockTarget::shared, target -> target),
                target -> target.canEncodeAsSharedRange()
                    ? Either.left(target.shared().orElseThrow())
                    : Either.right(target)
            );

        public StockTarget {
            shared = Objects.requireNonNull(shared, "shared");
            sell = Objects.requireNonNull(sell, "sell");
            buy = Objects.requireNonNull(buy, "buy");
            if (shared.isEmpty() && sell.isEmpty() && buy.isEmpty()) {
                throw new IllegalArgumentException("StockTarget must define shared, sell, or buy");
            }
        }

        public static StockTarget shared(StockRange range) {
            return new StockTarget(Optional.of(range), Optional.empty(), Optional.empty());
        }

        public static StockTarget directional(StockRange sell, StockRange buy) {
            return new StockTarget(Optional.empty(), Optional.of(sell), Optional.of(buy));
        }

        public boolean canEncodeAsSharedRange() {
            return shared.isPresent() && sell.isEmpty() && buy.isEmpty();
        }

        public StockRange toLegacyRange() {
            if (shared.isPresent()) {
                return shared.get();
            }
            if (sell.isPresent()) {
                return sell.get();
            }
            return buy.orElseThrow(() -> new IllegalStateException("StockTarget has no readable range"));
        }
    }

    /**
     * 库存范围
     */
    public record StockRange(
        int min,
        int max
    ) {
        public static final Codec<StockRange> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.INT.fieldOf("min").forGetter(StockRange::min),
                Codec.INT.fieldOf("max").forGetter(StockRange::max)
            ).apply(instance, StockRange::new)
        );
    }

    /**
     * 价格修正系数
     */
    public record PriceModifier(
        float sell,
        float buy
    ) {
        public static final Codec<PriceModifier> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.FLOAT.fieldOf("sell").forGetter(PriceModifier::sell),
                Codec.FLOAT.fieldOf("buy").forGetter(PriceModifier::buy)
            ).apply(instance, PriceModifier::new)
        );
    }

    // ==================== 交易契约相关数据结构 ====================

    /**
     * 货币篮组合策略
     */
    public enum CompositionStrategy {
        SMALLEST_ONLY("smallest_only"),
        LARGEST_FIRST("largest_first"),
        SINGLE("single");

        public static final Codec<CompositionStrategy> CODEC = Codec.STRING
            .xmap(CompositionStrategy::fromString, CompositionStrategy::getSerializedName);

        private final String serializedName;

        CompositionStrategy(String serializedName) {
            this.serializedName = serializedName;
        }

        public String getSerializedName() {
            return serializedName;
        }

        public static CompositionStrategy fromString(String name) {
            for (CompositionStrategy strategy : values()) {
                if (strategy.serializedName.equals(name)) {
                    return strategy;
                }
            }
            throw new IllegalArgumentException("Unknown composition strategy: " + name);
        }
    }

    /**
     * 契约条目基类（密封接口）
     */
    public sealed interface TradeContractEntry
        permits CurrencyBasketEntry, FixedTradeEntry {

        String typeString();

        /** 使用 dispatch 创建 Codec */
        @SuppressWarnings("unchecked")
        Codec<TradeContractEntry> CODEC = Codec.STRING.dispatch(
            "type",
            TradeContractEntry::typeString,
            type -> (MapCodec<TradeContractEntry>) (Object) mapCodecFor(type)
        );

        static MapCodec<? extends TradeContractEntry> mapCodecFor(String type) {
            return switch (type) {
                case "currency_basket_dynamic" -> CurrencyBasketEntry.MAP_CODEC;
                case "fixed" -> FixedTradeEntry.MAP_CODEC;
                default -> throw new IllegalArgumentException("Unknown contract type: " + type);
            };
        }
    }

    /**
     * 动态货币篮契约条目
     */
    public record CurrencyBasketEntry(
        TradeSide side,
        List<String> items,
        List<String> acceptedCurrencies,
        CompositionStrategy composition
    ) implements TradeContractEntry {

        public static final MapCodec<CurrencyBasketEntry> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                TradeSide.CODEC.fieldOf("side").forGetter(CurrencyBasketEntry::side),
                Codec.STRING.listOf().fieldOf("items").forGetter(CurrencyBasketEntry::items),
                Codec.STRING.listOf().fieldOf("accepted_currencies").forGetter(CurrencyBasketEntry::acceptedCurrencies),
                CompositionStrategy.CODEC.fieldOf("composition").forGetter(CurrencyBasketEntry::composition)
            ).apply(instance, CurrencyBasketEntry::new)
        );

        public static final Codec<CurrencyBasketEntry> CODEC = MAP_CODEC.codec();

        @Override
        public String typeString() {
            return "currency_basket_dynamic";
        }
    }

    /**
     * 固定交换契约条目
     */
    public record FixedTradeEntry(
        List<InputEntry> inputs,
        List<OutputEntry> outputs
    ) implements TradeContractEntry {

        public static final MapCodec<FixedTradeEntry> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                InputEntry.CODEC.listOf().fieldOf("inputs").forGetter(FixedTradeEntry::inputs),
                OutputEntry.CODEC.listOf().fieldOf("outputs").forGetter(FixedTradeEntry::outputs)
            ).apply(instance, FixedTradeEntry::new)
        );

        public static final Codec<FixedTradeEntry> CODEC = MAP_CODEC.codec();

        @Override
        public String typeString() {
            return "fixed";
        }
    }

    /**
     * 输入物品条目
     */
    public record InputEntry(
        String item,
        int count
    ) {
        public static final Codec<InputEntry> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.STRING.fieldOf("item").forGetter(InputEntry::item),
                Codec.INT.fieldOf("count").forGetter(InputEntry::count)
            ).apply(instance, InputEntry::new)
        );
    }

    /**
     * 输出物品条目
     */
    public record OutputEntry(
        String item,
        int count
    ) {
        public static final Codec<OutputEntry> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.STRING.fieldOf("item").forGetter(OutputEntry::item),
                Codec.INT.fieldOf("count").forGetter(OutputEntry::count)
            ).apply(instance, OutputEntry::new)
        );
    }
}
