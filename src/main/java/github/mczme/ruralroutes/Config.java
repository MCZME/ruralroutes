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
     * 周期时间模式
     */
    public enum CycleTimeMode {
        SERVER_TIME,
        GAME_TIME
    }

    /**
     * 周期时间模式配置
     */
    public static final ModConfigSpec.EnumValue<CycleTimeMode> CYCLE_TIME_MODE = BUILDER
            .comment("周期时间模式", "server_time: 基于服务器运行时间（实时）", "game_time: 基于游戏内昼夜循环")
            .defineEnum("cycle.time_mode", CycleTimeMode.SERVER_TIME);

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

    // ===== 市场系统配置 =====

    /**
     * 市场系统开关
     */
    public static final ModConfigSpec.BooleanValue MARKET_ENABLED = BUILDER
            .comment("是否启用市场系统", "启用后每个贸易周期会生成市场事件影响物品价格与库存基线")
            .define("market.enabled", true);

    /**
     * 每周期选择的规则数量
     */
    public static final ModConfigSpec.IntValue MARKET_RULE_PICK_COUNT = BUILDER
            .comment("每个贸易周期随机选择的市场事件规则数量", "数值越大，每周期可能出现的市场事件越多")
            .defineInRange("market.rule_pick_count", 3, 1, 20);

    /**
     * 最大价格调整幅度
     */
    public static final ModConfigSpec.DoubleValue MARKET_MAX_DELTA = BUILDER
            .comment("市场事件叠加后的最大价格调整百分比", "0.50 表示最多涨跌50%", "范围：0.1-1.0")
            .defineInRange("market.max_delta", 0.50, 0.1, 1.0);

    /**
     * 最大库存调整幅度
     */
    public static final ModConfigSpec.DoubleValue MARKET_MAX_STOCK_DELTA = BUILDER
            .comment("市场事件叠加后的最大库存调整百分比", "0.75 表示最多增减75%", "范围：0.0-1.0")
            .defineInRange("market.max_stock_delta", 0.75, 0.0, 1.0);

    static final ModConfigSpec SPEC = BUILDER.build();

    // ===== 工具方法 =====

    /**
     * 获取周期长度（tick）
     */
    public static long getCycleLengthInTicks() {
        return (long) CYCLE_DAYS.get() * 24000L;
    }
}
