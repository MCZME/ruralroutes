package github.mczme.ruralroutes;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 模组配置文件
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /**
     * 未定义价值规则的物品使用的默认价值
     */
    public static final ModConfigSpec.IntValue DEFAULT_VALUE = BUILDER
            .comment("未定义价值规则的物品使用的默认价值")
            .defineInRange("values.default", 1, 0, Integer.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();
}