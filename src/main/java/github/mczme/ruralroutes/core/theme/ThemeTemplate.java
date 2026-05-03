package github.mczme.ruralroutes.core.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
    Optional<Map<String, PriceModifier>> priceModifiers
) {
    public static final Codec<ThemeTemplate> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("name").forGetter(ThemeTemplate::name),
            ResourceLocation.CODEC.fieldOf("biome").forGetter(ThemeTemplate::biome),
            ItemReference.CODEC.listOf().fieldOf("sell_items").forGetter(ThemeTemplate::sellItems),
            ItemReference.CODEC.listOf().fieldOf("buy_items").forGetter(ThemeTemplate::buyItems),
            ResourceLocation.CODEC.listOf().optionalFieldOf("theme_specialties").forGetter(ThemeTemplate::themeSpecialties),
            StockConfig.CODEC.optionalFieldOf("stock").forGetter(ThemeTemplate::stock),
            Codec.unboundedMap(Codec.STRING, PriceModifier.CODEC).optionalFieldOf("price_modifiers").forGetter(ThemeTemplate::priceModifiers)
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
}