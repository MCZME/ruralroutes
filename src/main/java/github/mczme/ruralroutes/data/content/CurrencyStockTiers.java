package github.mczme.ruralroutes.data.content;

import github.mczme.ruralroutes.data.builder.CurrencyStockConfig;

/**
 * 主题货币库存分层。
 *
 * - PETTY: 零钱型，本地小额交易多，金币几乎不流通
 * - BULK: 大宗民生型，铜币吞吐高，铁币作为稳定结算
 * - CARAVAN: 商旅型，三种面额更均衡，便于补给和兑换
 * - WORKSHOP: 工坊型，铁币与少量金币更充足，适合高价值结算
 */
public final class CurrencyStockTiers {
    private CurrencyStockTiers() {
    }

    public static final CurrencyStockConfig PETTY = CurrencyStockConfig.of(
        24, 48, 80, 140,
        2, 5, 12, 24,
        0, 0, 0, 2
    );

    public static final CurrencyStockConfig BULK = CurrencyStockConfig.of(
        48, 80, 120, 200,
        4, 8, 20, 36,
        0, 1, 2, 4
    );

    public static final CurrencyStockConfig CARAVAN = CurrencyStockConfig.of(
        32, 56, 100, 160,
        6, 12, 24, 44,
        1, 3, 4, 10
    );

    public static final CurrencyStockConfig WORKSHOP = CurrencyStockConfig.of(
        20, 40, 80, 140,
        8, 14, 32, 56,
        1, 2, 4, 8
    );
}
