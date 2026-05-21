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
            ResourceLocation.CODEC.listOf().optionalFieldOf("theme_specialties").forGetter(ThemeTemplate::themeSpecialties),
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
    private final Optional<List<ResourceLocation>> themeSpecialties;
    private final Optional<StockConfig> stock;
    private final Optional<Map<String, PriceModifier>> priceModifiers;
    private final Optional<List<TradeContractEntry>> tradeContracts;
    private final Optional<List<ResourceLocation>> tradeProfiles;

    public ThemeTemplate(
        ResourceLocation name,
        ResourceLocation biome,
        List<ItemReference> sellItems,
        List<ItemReference> buyItems,
        Optional<List<ResourceLocation>> themeSpecialties,
        Optional<StockConfig> stock,
        Optional<Map<String, PriceModifier>> priceModifiers,
        Optional<List<TradeContractEntry>> tradeContracts
    ) {
        this(name, biome, sellItems, buyItems, themeSpecialties, stock, priceModifiers, tradeContracts, Optional.empty());
    }

    public ThemeTemplate(
        ResourceLocation name,
        ResourceLocation biome,
        List<ItemReference> sellItems,
        List<ItemReference> buyItems,
        Optional<List<ResourceLocation>> themeSpecialties,
        Optional<StockConfig> stock,
        Optional<Map<String, PriceModifier>> priceModifiers,
        Optional<List<TradeContractEntry>> tradeContracts,
        Optional<List<ResourceLocation>> tradeProfiles
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.biome = Objects.requireNonNull(biome, "biome");
        this.sellItems = List.copyOf(Objects.requireNonNull(sellItems, "sellItems"));
        this.buyItems = List.copyOf(Objects.requireNonNull(buyItems, "buyItems"));
        this.themeSpecialties = normalizeResourceListOptional(themeSpecialties);
        this.stock = Objects.requireNonNull(stock, "stock");
        this.priceModifiers = priceModifiers.map(map -> Map.copyOf(map));
        this.tradeContracts = tradeContracts.map(List::copyOf);
        this.tradeProfiles = normalizeResourceListOptional(tradeProfiles);
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

    public Optional<List<ResourceLocation>> themeSpecialties() {
        return themeSpecialties;
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

    private static Optional<List<ResourceLocation>> normalizeResourceListOptional(Optional<List<ResourceLocation>> value) {
        return value.map(List::copyOf);
    }

    /**
     * 物品引用，支持标签或精确物品。
     * 可使用纯字符串、单引用对象，或混合候选组对象。
     */
    public record ItemReference(
        Optional<String> id,           // 单引用，如 "#ruralroutes:pool/crop" 或 "minecraft:bread"
        Optional<List<String>> items,  // 混合候选组，可同时包含标签与精确物品
        Optional<Integer> pick,        // 展开候选后随机抽取的数量；缺省表示全部纳入
        Optional<String> key           // 可选来源键，供库存等后续逻辑稳定匹配
    ) {
        public ItemReference {
            id = id.map(String::strip).filter(str -> !str.isEmpty());
            items = items.map(list -> List.copyOf(list.stream()
                .map(String::strip)
                .filter(str -> !str.isEmpty())
                .toList()));
            key = key.map(String::strip).filter(str -> !str.isEmpty());

            boolean hasId = id.isPresent();
            boolean hasItems = items.isPresent();
            if (hasId == hasItems) {
                throw new IllegalArgumentException("ItemReference must define exactly one of id or items");
            }
            if (items.isPresent() && items.get().isEmpty()) {
                throw new IllegalArgumentException("ItemReference items cannot be empty");
            }
        }

        private static final Codec<ItemReference> OBJECT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.STRING.optionalFieldOf("id").forGetter(ItemReference::id),
                Codec.STRING.listOf().optionalFieldOf("items").forGetter(ItemReference::items),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("pick").forGetter(ItemReference::pick),
                Codec.STRING.optionalFieldOf("key").forGetter(ItemReference::key)
            ).apply(instance, ItemReference::new)
        );

        public static final Codec<ItemReference> CODEC = Codec.either(Codec.STRING, OBJECT_CODEC)
            .xmap(
                either -> either.map(
                    ItemReference::single,
                    ref -> ref
                ),
                ref -> ref.canUseStringShorthand()
                    ? Either.left(ref.id().orElseThrow())
                    : Either.right(ref)
            );

        public static ItemReference single(String id) {
            return new ItemReference(Optional.of(id), Optional.empty(), Optional.empty(), Optional.empty());
        }

        public static ItemReference single(String id, Integer pick) {
            return new ItemReference(Optional.of(id), Optional.empty(), Optional.ofNullable(pick), Optional.empty());
        }

        public static ItemReference group(List<String> refs, Integer pick, String key) {
            return new ItemReference(Optional.empty(), Optional.of(refs), Optional.ofNullable(pick), Optional.ofNullable(key));
        }

        /** 是否为单引用 */
        public boolean isSingle() {
            return id.isPresent();
        }

        /** 是否为候选组 */
        public boolean isGroup() {
            return items.isPresent();
        }

        /** 获取展开前的原始引用列表 */
        public List<String> refs() {
            return items.orElseGet(() -> List.of(id.orElseThrow()));
        }

        /** 获取运行时使用的稳定来源键 */
        public String sourceKey() {
            if (key.isPresent()) {
                return key.get();
            }
            if (id.isPresent()) {
                return id.get();
            }
            return "group:" + String.join("|", refs());
        }

        /** 获取用于日志的简短描述 */
        public String debugLabel() {
            return isSingle() ? id.orElse("<unknown>") : refs().toString();
        }

        /** 是否可序列化为字符串简写 */
        public boolean canUseStringShorthand() {
            return id.isPresent() && items.isEmpty() && pick.isEmpty() && key.isEmpty();
        }

        /** 是否为标签引用 */
        public boolean isTag() {
            return id.map(ref -> ref.startsWith("#")).orElse(false);
        }

        /** 获取物品/标签 ID（不含 # 前缀） */
        public String itemId() {
            String ref = id.orElseThrow(() -> new IllegalStateException("Grouped ItemReference has no single item id"));
            return isTag() ? ref.substring(1) : ref;
        }

        /** 是否在展开候选后进行抽样 */
        public boolean hasPickLimit() {
            return pick.isPresent();
        }
    }

    /**
     * 库存配置
     */
    public record StockConfig(
        Optional<StockRange> defaultRange,
        Optional<Map<String, StockRange>> targets
    ) {
        private static final Codec<StockConfigData> RAW_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                StockRange.CODEC.optionalFieldOf("default").forGetter(StockConfigData::defaultRange),
                Codec.unboundedMap(Codec.STRING, StockRange.CODEC).optionalFieldOf("targets").forGetter(StockConfigData::targets),
                Codec.unboundedMap(Codec.STRING, StockRange.CODEC).optionalFieldOf("specific").forGetter(StockConfigData::specific)
            ).apply(instance, StockConfigData::new)
        );

        public static final Codec<StockConfig> CODEC = RAW_CODEC.xmap(
            raw -> new StockConfig(
                raw.defaultRange(),
                raw.targets().isPresent() ? raw.targets() : raw.specific()
            ),
            config -> new StockConfigData(config.defaultRange(), config.targets(), Optional.empty())
        );

        public Optional<Map<String, StockRange>> specific() {
            return targets;
        }

        public Optional<Map<String, StockRange>> targets() {
            return targets;
        }

        private record StockConfigData(
            Optional<StockRange> defaultRange,
            Optional<Map<String, StockRange>> targets,
            Optional<Map<String, StockRange>> specific
        ) {}
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
