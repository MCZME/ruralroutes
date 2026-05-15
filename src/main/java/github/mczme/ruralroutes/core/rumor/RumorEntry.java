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
        RumorFamily family
) {

    private static final String SEPARATOR = "|";

    /**
     * 序列化为字符串
     */
    public String serialize() {
        return targetNameKey + SEPARATOR
                + scopeType.name() + SEPARATOR
                + scopeTargetName + SEPARATOR
                + family.name();
    }

    /**
     * 从字符串反序列化
     */
    public static Optional<RumorEntry> deserialize(String data) {
        String[] parts = data.split("\\|", -1);
        if (parts.length != 4) return Optional.empty();
        try {
            MarketScopeType scopeType = MarketScopeType.valueOf(parts[1]);
            RumorFamily family = RumorFamily.valueOf(parts[3]);
            return Optional.of(new RumorEntry(parts[0], scopeType, parts[2], family));
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
        if (isGossip()) {
            return Component.translatable(targetNameKey);
        }

        String templateKey = pickTemplateKey(random);
        Component target = Component.translatable(targetNameKey);
        return switch (scopeType) {
            case GLOBAL -> Component.translatable(templateKey, target);
            case BIOME, THEME -> Component.translatable(templateKey, getScopeTargetComponent(), target);
        };
    }

    private boolean isGossip() {
        return targetNameKey.startsWith("rumor.gossip.");
    }

    private Component getScopeTargetComponent() {
        if (scopeTargetName.isEmpty()) {
            return Component.empty();
        }
        return Component.translatable(scopeTargetName);
    }

    private String pickTemplateKey(Random random) {
        String[] templateKeys = switch (family) {
            case SHORTAGE -> switch (scopeType) {
                case GLOBAL -> new String[] {
                        "rumor.shortage.global.1",
                        "rumor.shortage.global.2"
                };
                case BIOME -> new String[] {
                        "rumor.shortage.biome.1",
                        "rumor.shortage.biome.2"
                };
                case THEME -> new String[] {
                        "rumor.shortage.theme.1",
                        "rumor.shortage.theme.2"
                };
            };
            case SURPLUS -> switch (scopeType) {
                case GLOBAL -> new String[] {
                        "rumor.surplus.global.1",
                        "rumor.surplus.global.2"
                };
                case BIOME -> new String[] {
                        "rumor.surplus.biome.1",
                        "rumor.surplus.biome.2"
                };
                case THEME -> new String[] {
                        "rumor.surplus.theme.1",
                        "rumor.surplus.theme.2"
                };
            };
            case DEMAND -> switch (scopeType) {
                case GLOBAL -> new String[] {
                        "rumor.demand.global.1",
                        "rumor.demand.global.2"
                };
                case BIOME -> new String[] {
                        "rumor.demand.biome.1",
                        "rumor.demand.biome.2"
                };
                case THEME -> new String[] {
                        "rumor.demand.theme.1",
                        "rumor.demand.theme.2"
                };
            };
            case RELEASE -> switch (scopeType) {
                case GLOBAL -> new String[] {
                        "rumor.release.global.1",
                        "rumor.release.global.2"
                };
                case BIOME -> new String[] {
                        "rumor.release.biome.1",
                        "rumor.release.biome.2"
                };
                case THEME -> new String[] {
                        "rumor.release.theme.1",
                        "rumor.release.theme.2"
                };
            };
        };

        return templateKeys[random.nextInt(templateKeys.length)];
    }

    /**
     * 创建闲谈情报（无市场事件时使用）
     */
    public static RumorEntry gossip(String gossipKey) {
        return new RumorEntry(gossipKey, MarketScopeType.GLOBAL, "", RumorFamily.RELEASE);
    }
}
