package github.mczme.ruralroutes.data.builder;

import github.mczme.ruralroutes.core.theme.StockRange;

/**
 * 主题货币库存配置。
 *
 * sell 表示村庄当前可支付的现货，
 * buy 表示村庄本周期还能继续吸纳的货币容量。
 */
public record CurrencyStockConfig(
    StockRange copperSell,
    StockRange copperBuy,
    StockRange ironSell,
    StockRange ironBuy,
    StockRange goldSell,
    StockRange goldBuy
) {
    public static CurrencyStockConfig of(
        int copperSellMin, int copperSellMax, int copperBuyMin, int copperBuyMax,
        int ironSellMin, int ironSellMax, int ironBuyMin, int ironBuyMax,
        int goldSellMin, int goldSellMax, int goldBuyMin, int goldBuyMax
    ) {
        return new CurrencyStockConfig(
            new StockRange(copperSellMin, copperSellMax),
            new StockRange(copperBuyMin, copperBuyMax),
            new StockRange(ironSellMin, ironSellMax),
            new StockRange(ironBuyMin, ironBuyMax),
            new StockRange(goldSellMin, goldSellMax),
            new StockRange(goldBuyMin, goldBuyMax)
        );
    }
}
