package github.mczme.ruralroutes.core.trade;

import com.mojang.serialization.Codec;

/**
 * 交易方向枚举
 * 定义价格计算时的交易方向，影响使用哪个主题修正系数
 */
public enum TradeSide {
    /** 村庄出售给玩家，使用 sellModifier */
    SELL_TO_PLAYER("sell_to_player"),
    /** 村庄从玩家收购，使用 buyModifier */
    BUY_FROM_PLAYER("buy_from_player");

    public static final Codec<TradeSide> CODEC = Codec.STRING
        .xmap(TradeSide::fromString, TradeSide::getSerializedName);

    private final String serializedName;

    TradeSide(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static TradeSide fromString(String name) {
        for (TradeSide side : values()) {
            if (side.serializedName.equals(name)) {
                return side;
            }
        }
        throw new IllegalArgumentException("Unknown trade side: " + name);
    }
}