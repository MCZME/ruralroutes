package github.mczme.ruralroutes.core.rumor;

import github.mczme.ruralroutes.core.market.MarketScopeType;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.Random;

/**
 * 情报条目
 *
 * 将市场事件转换为玩家可读的情报。
 * 告诉玩家：什么东西在哪里发生了什么变化。
 */
public record RumorEntry(
        String targetNameKey,
        MarketScopeType scopeType,
        String scopeTargetName,
        String directionKey
) {

    private static final String[] TEMPLATE_KEYS = {
            "rumor.template.1",
            "rumor.template.2",
            "rumor.template.3",
            "rumor.template.4",
            "rumor.template.5"
    };

    private static final String SEPARATOR = "|";

    /**
     * 序列化为字符串
     */
    public String serialize() {
        return targetNameKey + SEPARATOR
                + scopeType.name() + SEPARATOR
                + scopeTargetName + SEPARATOR
                + directionKey;
    }

    /**
     * 从字符串反序列化
     */
    public static Optional<RumorEntry> deserialize(String data) {
        String[] parts = data.split("\\|", -1);
        if (parts.length != 4) return Optional.empty();
        try {
            MarketScopeType scopeType = MarketScopeType.valueOf(parts[1]);
            return Optional.of(new RumorEntry(parts[0], scopeType, parts[2], parts[3]));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * 获取完整的显示文本
     * @param random 随机数生成器，用于选择模板
     * @return 组合后的情报文本
     */
    public Component getDisplayText(Random random) {
        // 随机选择模板
        String templateKey = TEMPLATE_KEYS[random.nextInt(TEMPLATE_KEYS.length)];

        // 物品/标签名称
        Component target = Component.translatable(targetNameKey);

        // 作用域范围
        Component scope;
        if (scopeType == MarketScopeType.GLOBAL) {
            scope = Component.translatable("rumor.scope.global");
        } else if (scopeType == MarketScopeType.BIOME) {
            scope = Component.translatable("rumor.scope.biome", scopeTargetName);
        } else {
            scope = Component.translatable("rumor.scope.theme", scopeTargetName);
        }

        // 涨跌
        Component direction = Component.translatable("rumor.direction." + directionKey);

        return Component.translatable(templateKey, target, scope, direction);
    }

    /**
     * 创建闲谈情报（无市场事件时使用）
     */
    public static RumorEntry gossip(String gossipKey) {
        return new RumorEntry(gossipKey, MarketScopeType.GLOBAL, "", "stable");
    }
}
