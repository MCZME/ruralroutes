package github.mczme.ruralroutes.core.rumor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

/**
 * 传闻语义族
 *
 * 不再直接用“上涨/下跌”套统一模板，而是先表达这条消息属于哪类商路传闻。
 */
public enum RumorFamily {
    SHORTAGE,
    SURPLUS,
    DEMAND,
    RELEASE;

    public static final Codec<RumorFamily> CODEC = Codec.STRING.flatXmap(
            name -> {
                try {
                    return DataResult.success(valueOf(name.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "Invalid rumor family: " + name);
                }
            },
            family -> DataResult.success(family.name().toLowerCase(Locale.ROOT))
    );

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }
}
