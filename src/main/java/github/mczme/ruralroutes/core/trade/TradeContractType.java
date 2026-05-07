package github.mczme.ruralroutes.core.trade;

import com.mojang.serialization.Codec;

/**
 * 契约类型枚举
 */
public enum TradeContractType {
    CURRENCY_BASKET_DYNAMIC("currency_basket_dynamic"),
    FIXED("fixed"),
    COIN_EXCHANGE("coin_exchange");

    public static final Codec<TradeContractType> CODEC = Codec.STRING
        .xmap(TradeContractType::fromString, TradeContractType::getSerializedName);

    private final String serializedName;

    TradeContractType(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static TradeContractType fromString(String name) {
        for (TradeContractType type : values()) {
            if (type.serializedName.equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown contract type: " + name);
    }
}
