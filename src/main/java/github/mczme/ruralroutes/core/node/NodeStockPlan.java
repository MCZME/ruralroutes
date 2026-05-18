package github.mczme.ruralroutes.core.node;

/**
 * 节点库存初始化阶段的出售现货/收购容量基线。
 *
 * 同一物品同时出现在买卖两侧时，current 表示当前现货量，
 * max 表示总容量（现货量 + 可收购空间）。
 */
record NodeStockPlan(
        int sellBase,
        int buyBase
) {
    static final NodeStockPlan EMPTY = new NodeStockPlan(0, 0);

    NodeStockPlan addSell(int amount) {
        return new NodeStockPlan(sellBase + amount, buyBase);
    }

    NodeStockPlan addBuy(int amount) {
        return new NodeStockPlan(sellBase, buyBase + amount);
    }

    StockEntry toStockEntry() {
        if (sellBase <= 0 && buyBase <= 0) {
            return StockEntry.empty(0);
        }
        return new StockEntry(sellBase, sellBase + buyBase);
    }
}
