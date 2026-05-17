package github.mczme.ruralroutes.core.market;

import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * 市场上下文
 * 用于查询价格调整时提供的上下文信息。
 * 包含主题和群系的标识，用于匹配市场事件的作用域。
 */
public record MarketContext(
        Optional<ResourceLocation> themeId,
        Optional<ResourceLocation> biomeId
) {
    /**
     * 创建空上下文
     * @return 不包含主题和群系信息的上下文
     */
    public static MarketContext empty() {
        return new MarketContext(Optional.empty(), Optional.empty());
    }

    /**
     * 创建完整上下文
     * @param themeId 主题 ID
     * @param biomeId 群系 ID
     * @return 包含完整信息的上下文
     */
    public static MarketContext of(ResourceLocation themeId, ResourceLocation biomeId) {
        return new MarketContext(Optional.of(themeId), Optional.of(biomeId));
    }

    /**
     * 从主题模板创建上下文
     */
    public static MarketContext fromTheme(ThemeTemplate template) {
        if (template == null) {
            return empty();
        }
        return of(template.name(), template.biome());
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
