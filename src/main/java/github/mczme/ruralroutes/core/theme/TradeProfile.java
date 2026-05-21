package github.mczme.ruralroutes.core.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 交易配置片段。
 * 主题可以引用多个 profile，按声明顺序拼接。
 */
public record TradeProfile(
    ResourceLocation name,
    List<ThemeTemplate.ItemReference> sellItems,
    List<ThemeTemplate.ItemReference> buyItems,
    Optional<List<ResourceLocation>> themeSpecialties,
    Optional<ThemeTemplate.StockConfig> stock,
    Optional<Map<String, ThemeTemplate.PriceModifier>> priceModifiers,
    Optional<List<ThemeTemplate.TradeContractEntry>> tradeContracts
) {
    public static final Codec<TradeProfile> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("name").forGetter(TradeProfile::name),
            ThemeTemplate.ItemReference.CODEC.listOf().fieldOf("sell_items").forGetter(TradeProfile::sellItems),
            ThemeTemplate.ItemReference.CODEC.listOf().fieldOf("buy_items").forGetter(TradeProfile::buyItems),
            ResourceLocation.CODEC.listOf().optionalFieldOf("theme_specialties").forGetter(TradeProfile::themeSpecialties),
            ThemeTemplate.StockConfig.CODEC.optionalFieldOf("stock").forGetter(TradeProfile::stock),
            Codec.unboundedMap(Codec.STRING, ThemeTemplate.PriceModifier.CODEC).optionalFieldOf("price_modifiers").forGetter(TradeProfile::priceModifiers),
            ThemeTemplate.TradeContractEntry.CODEC.listOf().optionalFieldOf("trade_contracts").forGetter(TradeProfile::tradeContracts)
        ).apply(instance, TradeProfile::new)
    );

    public TradeProfile {
        name = Objects.requireNonNull(name, "name");
        sellItems = List.copyOf(Objects.requireNonNull(sellItems, "sellItems"));
        buyItems = List.copyOf(Objects.requireNonNull(buyItems, "buyItems"));
        themeSpecialties = themeSpecialties.map(List::copyOf);
        priceModifiers = priceModifiers.map(Map::copyOf);
        tradeContracts = tradeContracts.map(List::copyOf);
    }

}
