package github.mczme.ruralroutes.core.market;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * 市场状态
 *
 * 存储当前贸易周期的所有市场事件快照。
 * 市场状态在贸易周期刷新时生成，整个周期内保持不变。
 */
public record MarketState(
        long cycleIndex,
        List<MarketEvent> events
) {
    public static final Codec<MarketState> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.LONG.fieldOf("cycle_index").forGetter(MarketState::cycleIndex),
                    MarketEvent.CODEC.listOf().fieldOf("events").forGetter(MarketState::events)
            ).apply(instance, MarketState::new)
    );

    /**
     * 创建空的市场状态
     * @param cycleIndex 周期索引
     * @return 不包含任何事件的空状态
     */
    public static MarketState empty(long cycleIndex) {
        return new MarketState(cycleIndex, List.of());
    }

    /**
     * 检查是否为空状态
     * @return 如果没有任何事件则返回 true
     */
    public boolean isEmpty() {
        return events.isEmpty();
    }

    /**
     * 获取指定作用域类型的所有事件
     * @param scopeType 作用域类型
     * @return 该作用域类型的事件列表
     */
    public List<MarketEvent> getEventsByScopeType(MarketScopeType scopeType) {
        return events.stream()
                .filter(event -> event.scopeType() == scopeType)
                .toList();
    }

    /**
     * 获取事件总数
     * @return 事件数量
     */
    public int getEventCount() {
        return events.size();
    }
}
