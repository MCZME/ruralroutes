package github.mczme.ruralroutes.core.trade;

/**
 * 交易方向枚举
 * 定义价格计算时的交易方向，影响使用哪个主题修正系数
 */
public enum TradeSide {
    /** 村庄出售给玩家，使用 sellModifier */
    SELL_TO_PLAYER,
    /** 村庄从玩家收购，使用 buyModifier */
    BUY_FROM_PLAYER
}