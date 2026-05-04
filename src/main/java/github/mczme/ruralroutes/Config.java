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

    // ===== 贸易周期配置 =====

    /**
     * 贸易周期长度（游戏日）
     */
    public static final ModConfigSpec.IntValue CYCLE_DAYS = BUILDER
            .comment("贸易周期长度，单位为游戏日", "建议范围 1-30")
            .defineInRange("cycle.days", 3, 1, 30);

    /**
     * 刷新时刻
     */
    public static final ModConfigSpec.EnumValue<RefreshTime> REFRESH_TIME = BUILDER
            .comment("贸易周期刷新时刻", "sunrise=日出(24000), noon=正午(6000), sunset=日落(12000), midnight=午夜(18000)")
            .defineEnum("cycle.refresh_time", RefreshTime.SUNRISE);

    /**
     * 刷新时刻枚举
     */
    public enum RefreshTime {
        SUNRISE(24000),
        NOON(6000),
        SUNSET(12000),
        MIDNIGHT(18000);

        private final int tickOffset;

        RefreshTime(int tickOffset) {
            this.tickOffset = tickOffset;
        }

        public int getTickOffset() {
            return tickOffset;
        }
    }

    static final ModConfigSpec SPEC = BUILDER.build();

    // ===== 工具方法 =====

    /**
     * 获取周期长度（tick）
     */
    public static long getCycleLengthInTicks() {
        return (long) CYCLE_DAYS.get() * 24000L;
    }
}