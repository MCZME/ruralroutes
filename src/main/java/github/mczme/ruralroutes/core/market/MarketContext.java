package github.mczme.ruralroutes.core.market;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * 市场上下文
 *
 * 用于查询价格调整时提供的上下文信息。
 * 包含交易节点、主题和群系的标识。
 */
public record MarketContext(
        ResourceLocation tradeNodeId,
        Optional<ResourceLocation> themeId,
        Optional<ResourceLocation> biomeId
) {
    /**
     * 创建仅含节点 ID 的上下文
     * @param tradeNodeId 交易节点 ID
     * @return 不包含主题和群系信息的上下文
     */
    public static MarketContext ofNode(ResourceLocation tradeNodeId) {
        return new MarketContext(tradeNodeId, Optional.empty(), Optional.empty());
    }

    /**
     * 创建完整上下文
     * @param tradeNodeId 交易节点 ID
     * @param themeId 主题 ID
     * @param biomeId 群系 ID
     * @return 包含完整信息的上下文
     */
    public static MarketContext of(ResourceLocation tradeNodeId,
                                    ResourceLocation themeId,
                                    ResourceLocation biomeId) {
        return new MarketContext(tradeNodeId, Optional.of(themeId), Optional.of(biomeId));
    }

    /**
     * 检查是否匹配指定群系
     * @param biomeId 目标群系 ID
     * @return 如果上下文包含相同群系则返回 true
     */
    public boolean matchesBiome(ResourceLocation biomeId) {
        return this.biomeId.map(id -> id.equals(biomeId)).orElse(false);
    }

    /**
     * 检查是否匹配指定主题
     * @param themeId 目标主题 ID
     * @return 如果上下文包含相同主题则返回 true
     */
    public boolean matchesTheme(ResourceLocation themeId) {
        return this.themeId.map(id -> id.equals(themeId)).orElse(false);
    }
}