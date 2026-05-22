package github.mczme.ruralroutes.core.theme;

import com.mojang.serialization.Codec;

/**
 * 货币篮组合策略
 */
public enum CompositionStrategy {
    SMALLEST_ONLY("smallest_only"),
    LARGEST_FIRST("largest_first"),
    SINGLE("single");

    public static final Codec<CompositionStrategy> CODEC = Codec.STRING
        .xmap(CompositionStrategy::fromString, CompositionStrategy::getSerializedName);

    private final String serializedName;

    CompositionStrategy(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static CompositionStrategy fromString(String name) {
        for (CompositionStrategy strategy : values()) {
            if (strategy.serializedName.equals(name)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unknown composition strategy: " + name);
    }
}
