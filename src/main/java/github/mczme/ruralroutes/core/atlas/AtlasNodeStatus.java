package github.mczme.ruralroutes.core.atlas;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * 商路图册节点状态。
 */
public enum AtlasNodeStatus implements StringRepresentable {
    CLUE("clue"),
    RECORDED("recorded"),
    INVALID("invalid");

    public static final Codec<AtlasNodeStatus> CODEC =
        Codec.STRING.xmap(AtlasNodeStatus::byName, AtlasNodeStatus::getSerializedName);

    private final String serializedName;

    AtlasNodeStatus(String serializedName) {
        this.serializedName = serializedName;
    }

    public String translationKey() {
        return "gui.ruralroutes.trade_atlas.status." + serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static AtlasNodeStatus byName(String serializedName) {
        return switch (serializedName) {
            case "clue" -> CLUE;
            case "recorded" -> RECORDED;
            case "invalid" -> INVALID;
            default -> CLUE;
        };
    }
}
