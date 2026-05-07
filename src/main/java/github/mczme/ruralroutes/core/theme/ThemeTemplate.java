package github.mczme.ruralroutes.core.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.core.trade.TradeSide;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 主题模板数据
 * 定义村庄的出售/收购物品、特产、库存和价格修正
 */
public record ThemeTemplate(
    ResourceLocation name,
    ResourceLocation biome,
    List<ItemReference> sellItems,
    List<ItemReference> buyItems,
    Optional<List<ResourceLocation>> themeSpecialties,
    Optional<StockConfig> stock,
    Optional<Map<String, PriceModifier>> priceModifiers,
    Optional<List<TradeContractEntry>> tradeContracts
) {
    public static final Codec<ThemeTemplate> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("name").forGetter(ThemeTemplate::name),
            ResourceLocation.CODEC.fieldOf("biome").forGetter(ThemeTemplate::biome),
            ItemReference.CODEC.listOf().fieldOf("sell_items").forGetter(ThemeTemplate::sellItems),
            ItemReference.CODEC.listOf().fieldOf("buy_items").forGetter(ThemeTemplate::buyItems),
            ResourceLocation.CODEC.listOf().optionalFieldOf("theme_specialties").forGetter(ThemeTemplate::themeSpecialties),
            StockConfig.CODEC.optionalFieldOf("stock").forGetter(ThemeTemplate::stock),
            Codec.unboundedMap(Codec.STRING, PriceModifier.CODEC).optionalFieldOf("price_modifiers").forGetter(ThemeTemplate::priceModifiers),
            TradeContractEntry.CODEC.listOf().optionalFieldOf("trade_contracts").forGetter(ThemeTemplate::tradeContracts)
        ).apply(instance, ThemeTemplate::new)
    );

    /**
     * 物品引用，支持标签或精确物品
     * 通过 # 前缀标识标签，如 "#ruralroutes:pool/crop" 或 "minecraft:bread"
     */
    public record ItemReference(
        String id     // 如 "#ruralroutes:pool/crop" 或 "minecraft:bread"
    ) {
        public static final Codec<ItemReference> CODEC = Codec.STRING
            .xmap(ItemReference::new, ItemReference::id);

        /** 是否为标签引用 */
        public boolean isTag() {
            return id.startsWith("#");
        }

        /** 获取物品/标签 ID（不含 # 前缀） */
        public String itemId() {
            return isTag() ? id.substring(1) : id;
        }
    }

    /**
     * 库存配置
     */
    public record StockConfig(
        Optional<StockRange> defaultRange,
        Optional<Map<String, StockRange>> specific
    ) {
        public static final Codec<StockConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                StockRange.CODEC.optionalFieldOf("default").forGetter(StockConfig::defaultRange),
                Codec.unboundedMap(Codec.STRING, StockRange.CODEC).optionalFieldOf("specific").forGetter(StockConfig::specific)
            ).apply(instance, StockConfig::new)
        );
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

        /** 使用 dispatchMap 创建 Codec */
        Codec<TradeContractEntry> CODEC = Codec.STRING.dispatch(
            "type",
            TradeContractEntry::typeString,
            type -> (MapCodec<TradeContractEntry>) mapCodecFor(type)
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