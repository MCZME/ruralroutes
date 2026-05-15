package github.mczme.ruralroutes.core.rumor;

import github.mczme.ruralroutes.core.market.MarketEvent;
import github.mczme.ruralroutes.core.market.MarketScopeType;
import github.mczme.ruralroutes.core.market.MarketState;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Random;

/**
 * 情报生成器
 *
 * 将市场状态转换为情报条目列表。
 */
public final class RumorGenerator {

    private static final String[] GOSSIP_KEYS = {
            "rumor.gossip.1",
            "rumor.gossip.2",
            "rumor.gossip.3"
    };

    private RumorGenerator() {}

    /**
     * 从市场状态生成情报列表
     * @param state 市场状态
     * @param random 随机数生成器
     * @return 情报条目列表，无事件时返回闲谈情报
     */
    public static List<RumorEntry> generateFromMarketState(MarketState state, Random random) {
        if (state.isEmpty()) {
            return List.of(generateGossipRumor(random));
        }
        return state.events().stream()
                .map(RumorGenerator::convertEventToRumor)
                .toList();
    }

    /**
     * 生成闲谈情报
     */
    public static RumorEntry generateGossipRumor(Random random) {
        return RumorEntry.gossip(GOSSIP_KEYS[random.nextInt(GOSSIP_KEYS.length)]);
    }

    private static RumorEntry convertEventToRumor(MarketEvent event) {
        // 1. 目标名称翻译键
        String targetNameKey = resolveTargetNameKey(event);

        // 2. 作用域目标名称
        String scopeTargetName = resolveScopeTargetName(event);

        return new RumorEntry(
                targetNameKey,
                event.scopeType(),
                scopeTargetName,
                event.rumorFamily()
        );
    }

    /**
     * 解析目标名称翻译键
     */
    private static String resolveTargetNameKey(MarketEvent event) {
        if (event.rumorTargetKey().isPresent()) {
            return event.rumorTargetKey().get();
        }

        if (event.isTargetTag()) {
            // 标签: ruralroutes.tag.{namespace}.{path}
            ResourceLocation tagId = ResourceLocation.parse(event.getTargetId());
            return "ruralroutes.tag." + tagId.getNamespace() + "." + tagId.getPath();
        } else {
            // 物品: 直接使用物品翻译键
            ResourceLocation itemId = ResourceLocation.parse(event.getTargetId());
            return "item." + itemId.getNamespace() + "." + itemId.getPath();
        }
    }

    /**
     * 解析作用域目标名称
     */
    private static String resolveScopeTargetName(MarketEvent event) {
        if (event.scopeType() == MarketScopeType.GLOBAL) {
            return "";
        }
        return event.scopeTarget()
                .map(loc -> getScopeTranslationKey(event.scopeType(), loc))
                .orElse("");
    }

    /**
     * 获取作用域翻译键
     */
    private static String getScopeTranslationKey(MarketScopeType type, ResourceLocation loc) {
        if (type == MarketScopeType.BIOME) {
            // 群系: 使用 Minecraft 内置翻译键格式
            return "biome." + loc.getNamespace() + "." + loc.getPath();
        } else {
            // 主题: ruralroutes.theme.{name}
            return "ruralroutes.theme." + loc.getPath();
        }
    }
}
