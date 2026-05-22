package github.mczme.ruralroutes.core.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.core.trade.TradeSide;

import java.util.List;

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
