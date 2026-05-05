package github.mczme.ruralroutes.core.market;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

/**
 * 市场作用域类型枚举
 *
 * 定义市场事件的影响范围：
 * - GLOBAL: 全局生效，影响所有交易
 * - BIOME: 指定群系生效，影响特定群系的交易
 * - THEME: 指定主题生效，影响特定主题的交易
 */
public enum MarketScopeType {
    GLOBAL,
    BIOME,
    THEME;

    public static final Codec<MarketScopeType> CODEC = Codec.STRING.flatXmap(
            name -> {
                try {
                    return DataResult.success(valueOf(name.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "Invalid scope type: " + name);
                }
            },
            type -> DataResult.success(type.name().toLowerCase(Locale.ROOT))
    );
}
