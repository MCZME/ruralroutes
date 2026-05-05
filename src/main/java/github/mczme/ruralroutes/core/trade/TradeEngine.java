package github.mczme.ruralroutes.core.trade;

/**
 * 交易引擎接口
 * 处理贸易站交易执行
 */
public interface TradeEngine {

    /**
     * 获取单例实例
     */
    static TradeEngine getInstance() {
        return TradeEngineImpl.INSTANCE;
    }
}
