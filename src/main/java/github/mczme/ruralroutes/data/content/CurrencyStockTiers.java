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
        300, 360, 120, 180,
        80, 96, 32, 48,
        4, 4, 2, 4
    );

    public static final CurrencyStockConfig BULK = CurrencyStockConfig.of(
        360, 480, 160, 240,
        80, 112, 48, 72,
        4, 6, 4, 8
    );

    public static final CurrencyStockConfig CARAVAN = CurrencyStockConfig.of(
        320, 420, 140, 220,
        80, 104, 40, 64,
        4, 6, 4, 8
    );

    public static final CurrencyStockConfig WORKSHOP = CurrencyStockConfig.of(
        300, 400, 120, 200,
        80, 120, 64, 96,
        4, 8, 6, 12
    );
}
