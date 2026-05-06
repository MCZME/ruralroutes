package github.mczme.ruralroutes.data.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import net.minecraft.resources.ResourceLocation;

/**
 * 主题模板构建器
 * 简化 ThemeDataProvider 中的主题定义
 */
public class ThemeBuilder {

    private final String name;
    private String biome;
    private final List<ThemeTemplate.ItemReference> sellItems = new ArrayList<>();
    private final List<ThemeTemplate.ItemReference> buyItems = new ArrayList<>();
    private final List<ResourceLocation> specialties = new ArrayList<>();
    private ThemeTemplate.StockRange defaultStock;
    private final Map<String, ThemeTemplate.StockRange> stockSpecific = new HashMap<>();
    private final Map<String, ThemeTemplate.PriceModifier> priceModifiers = new HashMap<>();
    private boolean withCurrency = false;

    private BiConsumer<ResourceLocation, ThemeTemplate> registrar;

    private ThemeBuilder(String name) {
        this.name = name;
    }

    public static ThemeBuilder create(String name) {
        return new ThemeBuilder(name);
    }

    /**
     * 添加默认货币到出售池和收购池
     * 村庄会出售和收购铜板（基础货币）
     */
    public ThemeBuilder withCurrency() {
        this.withCurrency = true;
        return this;
    }

    /**
     * 设置注册器（由 ThemeDataProvider 调用）
     */
    public ThemeBuilder registrar(BiConsumer<ResourceLocation, ThemeTemplate> registrar) {
        this.registrar = registrar;
        return this;
    }

    public ThemeBuilder biome(String biome) {
        this.biome = biome;
        return this;
    }

    /**
     * 添加出售物品
     * 格式: "tag:xxx" 表示标签，其他为物品ID
     */
    public ThemeBuilder sell(String... items) {
        for (String item : items) {
            sellItems.add(parseItemRef(item));
        }
        return this;
    }

    /**
     * 添加收购物品
     */
    public ThemeBuilder buy(String... items) {
        for (String item : items) {
            buyItems.add(parseItemRef(item));
        }
        return this;
    }

    /**
     * 添加主题特产
     */
    public ThemeBuilder specialty(String... items) {
        for (String item : items) {
            specialties.add(ResourceLocation.parse(item));
        }
        return this;
    }

    /**
     * 设置默认库存范围
     */
    public ThemeBuilder stock(int min, int max) {
        this.defaultStock = new ThemeTemplate.StockRange(min, max);
        return this;
    }

    /**
     * 添加特定物品库存配置
     */
    public ThemeBuilder stockSpecific(String key, int min, int max) {
        stockSpecific.put(key, new ThemeTemplate.StockRange(min, max));
        return this;
    }

    /**
     * 添加价格修正
     */
    public ThemeBuilder priceModifier(String key, float sell, float buy) {
        priceModifiers.put(key, new ThemeTemplate.PriceModifier(sell, buy));
        return this;
    }

    /**
     * 获取资源位置
     */
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, name);
    }

    /**
     * 构建 ThemeTemplate
     */
    public ThemeTemplate build() {
        List<ThemeTemplate.ItemReference> finalSellItems = new ArrayList<>(sellItems);
        List<ThemeTemplate.ItemReference> finalBuyItems = new ArrayList<>(buyItems);

        // 添加默认货币到出售池和收购池
        if (withCurrency) {
            finalSellItems.add(new ThemeTemplate.ItemReference("#ruralroutes:currency"));
            finalBuyItems.add(new ThemeTemplate.ItemReference("#ruralroutes:currency"));
        }

        return new ThemeTemplate(
            getId(),
            ResourceLocation.parse(biome),
            List.copyOf(finalSellItems),
            List.copyOf(finalBuyItems),
            specialties.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(specialties)),
            defaultStock == null && stockSpecific.isEmpty()
                ? Optional.empty()
                : Optional.of(new ThemeTemplate.StockConfig(
                    Optional.ofNullable(defaultStock),
                    stockSpecific.isEmpty() ? Optional.empty() : Optional.of(Map.copyOf(stockSpecific))
                )),
            priceModifiers.isEmpty() ? Optional.empty() : Optional.of(Map.copyOf(priceModifiers))
        );
    }

    /**
     * 构建并注册主题
     */
    public void register() {
        if (registrar != null) {
            registrar.accept(getId(), build());
        }
    }

    /**
     * 解析物品引用字符串
     * "tag:xxx" -> "#xxx"（标签）
     * "xxx" -> "xxx"（物品）
     */
    private static ThemeTemplate.ItemReference parseItemRef(String str) {
        if (str.startsWith("tag:")) {
            String tagId = str.substring(4);
            // 如果已经有#前缀则保留，否则添加
            if (!tagId.startsWith("#")) {
                tagId = "#" + tagId;
            }
            return new ThemeTemplate.ItemReference(tagId);
        } else {
            return new ThemeTemplate.ItemReference(str);
        }
    }
}