package github.mczme.ruralroutes.core.atlas;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * 玩家整理商路时使用的固定状态。
 */
public enum TradeRouteStatus implements StringRepresentable {
    DRAFT("draft"),
    REGULAR("regular"),
    PAUSED("paused");

    public static final Codec<TradeRouteStatus> CODEC =
        Codec.STRING.xmap(TradeRouteStatus::byName, TradeRouteStatus::getSerializedName);

    private final String serializedName;

    TradeRouteStatus(String serializedName) {
        this.serializedName = serializedName;
    }

    public TradeRouteStatus next() {
        return switch (this) {
            case DRAFT -> REGULAR;
            case REGULAR -> PAUSED;
            case PAUSED -> DRAFT;
        };
    }

    public String translationKey() {
        return "gui.ruralroutes.trade_atlas.route.status." + serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static TradeRouteStatus byName(String serializedName) {
        return switch (serializedName) {
            case "regular" -> REGULAR;
            case "paused" -> PAUSED;
            default -> DRAFT;
        };
    }
}
