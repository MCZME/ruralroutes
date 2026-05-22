package github.mczme.ruralroutes.core.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 交易配置片段。
 * 主题可以引用多个 profile，按声明顺序拼接。
 */
public record TradeProfile(
    ResourceLocation name,
    List<ItemReference> sellItems,
    List<ItemReference> buyItems,
    Optional<List<ItemReference>> themeSpecialties,
    Optional<StockConfig> stock,
    Optional<List<TradeContractEntry>> tradeContracts
) {
    public static final Codec<TradeProfile> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("name").forGetter(TradeProfile::name),
            ItemReference.CODEC.listOf().optionalFieldOf("sell_items", List.of()).forGetter(TradeProfile::sellItems),
            ItemReference.CODEC.listOf().optionalFieldOf("buy_items", List.of()).forGetter(TradeProfile::buyItems),
            ItemReference.CODEC.listOf().optionalFieldOf("theme_specialties").forGetter(TradeProfile::themeSpecialties),
            StockConfig.CODEC.optionalFieldOf("stock").forGetter(TradeProfile::stock),
            TradeContractEntry.CODEC.listOf().optionalFieldOf("trade_contracts").forGetter(TradeProfile::tradeContracts)
        ).apply(instance, TradeProfile::new)
    );

    public TradeProfile {
        name = Objects.requireNonNull(name, "name");
        sellItems = List.copyOf(Objects.requireNonNull(sellItems, "sellItems"));
        buyItems = List.copyOf(Objects.requireNonNull(buyItems, "buyItems"));
        themeSpecialties = themeSpecialties.map(List::copyOf);
        tradeContracts = tradeContracts.map(List::copyOf);
        if (stock.flatMap(StockConfig::defaultRange).isPresent()) {
            throw new IllegalArgumentException("TradeProfile stock.default is not allowed; use ThemeTemplate stock.default");
        }
    }

}
