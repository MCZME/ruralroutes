package github.mczme.ruralroutes.core.theme;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.core.trade.TradeTargetRef;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 主题模板原始数据。
 * 仅保留主题级字段；交易内容统一由一个或多个 TradeProfile 提供。
 */
public class ThemeTemplate {

    private static final Codec<List<PriceModifier>> PRICE_MODIFIERS_CODEC = Codec.either(
        PriceModifier.CODEC.listOf(),
        Codec.unboundedMap(Codec.STRING, LegacyPriceModifier.CODEC)
    ).xmap(
        either -> either.map(list -> list, map -> map.entrySet().stream()
            .map(entry -> PriceModifier.of(
                TradeTargetRef.fromString(entry.getKey()),
                entry.getValue().sell(),
                entry.getValue().buy()
            ))
            .toList()),
        list -> Either.left(list)
    );

    public static final Codec<ThemeTemplate> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("name").forGetter(ThemeTemplate::name),
            ResourceLocation.CODEC.fieldOf("biome").forGetter(ThemeTemplate::biome),
            StockConfig.CODEC.optionalFieldOf("stock").forGetter(ThemeTemplate::stock),
            PRICE_MODIFIERS_CODEC.optionalFieldOf("price_modifiers").forGetter(ThemeTemplate::priceModifiers),
            ResourceLocation.CODEC.listOf().optionalFieldOf("trade_profiles").forGetter(ThemeTemplate::tradeProfiles)
        ).apply(instance, ThemeTemplate::new)
    );

    private final ResourceLocation name;
    private final ResourceLocation biome;
    private final Optional<StockConfig> stock;
    private final Optional<List<PriceModifier>> priceModifiers;
    private final Optional<List<ResourceLocation>> tradeProfiles;

    public ThemeTemplate(
        ResourceLocation name,
        ResourceLocation biome,
        Optional<StockConfig> stock,
        Optional<List<PriceModifier>> priceModifiers,
        Optional<List<ResourceLocation>> tradeProfiles
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.biome = Objects.requireNonNull(biome, "biome");
        this.stock = Objects.requireNonNull(stock, "stock");
        this.priceModifiers = priceModifiers.map(List::copyOf);
        this.tradeProfiles = tradeProfiles.map(List::copyOf);
    }

    public ResourceLocation name() {
        return name;
    }

    public ResourceLocation biome() {
        return biome;
    }

    public Optional<StockConfig> stock() {
        return stock;
    }

    public Optional<List<PriceModifier>> priceModifiers() {
        return priceModifiers;
    }

    public Optional<List<ResourceLocation>> tradeProfiles() {
        return tradeProfiles;
    }

    /**
     * 旧版 price_modifiers 的兼容结构。
     *
     * 历史数据使用 target -> {sell, buy} 的对象映射；新模型改为有序数组后，
     * 这里保留只读解码，避免既有 generated 资源失效。
     */
    private record LegacyPriceModifier(
        float sell,
        float buy
    ) {
        private static final Codec<LegacyPriceModifier> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.FLOAT.fieldOf("sell").forGetter(LegacyPriceModifier::sell),
                Codec.FLOAT.fieldOf("buy").forGetter(LegacyPriceModifier::buy)
            ).apply(instance, LegacyPriceModifier::new)
        );
    }
}
