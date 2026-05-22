package github.mczme.ruralroutes.core.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

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
