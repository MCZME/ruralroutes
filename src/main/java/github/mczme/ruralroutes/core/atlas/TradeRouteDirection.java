package github.mczme.ruralroutes.core.atlas;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * 商路路段方向模式。
 */
public enum TradeRouteDirection implements StringRepresentable {
    ONE_WAY("one_way"),
    BIDIRECTIONAL("bidirectional");

    public static final Codec<TradeRouteDirection> CODEC =
        Codec.STRING.xmap(TradeRouteDirection::byName, TradeRouteDirection::getSerializedName);

    private final String serializedName;

    TradeRouteDirection(String serializedName) {
        this.serializedName = serializedName;
    }

    public TradeRouteDirection next() {
        return this == ONE_WAY ? BIDIRECTIONAL : ONE_WAY;
    }

    public String translationKey() {
        return "gui.ruralroutes.trade_atlas.route.direction." + serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static TradeRouteDirection byName(String serializedName) {
        return "bidirectional".equals(serializedName) ? BIDIRECTIONAL : ONE_WAY;
    }
}
