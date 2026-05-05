package github.mczme.ruralroutes.core.util;

import github.mczme.ruralroutes.RuralRoutes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 标签查询缓存工具类
 * 统一管理标签查询和缓存，在 TagsUpdatedEvent 时自动失效
 */
public final class TagLookupCache {

    private TagLookupCache() {}

    /** 缓存：标签引用字符串 -> 物品集合 */
    private static final Map<String, Set<Item>> TAG_CACHE = new ConcurrentHashMap<>();

    /**
     * 判断物品栈是否匹配引用
     * @param stack 物品栈
     * @param itemRef 引用字符串，如 "#minecraft:axes" 或 "minecraft:bread"
     * @return 是否匹配
     */
    public static boolean matchesItem(ItemStack stack, String itemRef) {
        if (stack.isEmpty()) {
            return false;
        }

        if (isTagRef(itemRef)) {
            // 标签引用：使用 ItemStack.is(TagKey) 检查
            String tagId = parseRefId(itemRef);
            try {
                ResourceLocation tagLocation = ResourceLocation.parse(tagId);
                TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagLocation);
                return stack.is(tagKey);
            } catch (Exception e) {
                RuralRoutes.LOGGER.warn("Invalid tag reference: {}", itemRef);
                return false;
            }
        } else {
            // 精确物品引用：比较物品 ID
            try {
                ResourceLocation itemId = ResourceLocation.parse(itemRef);
                return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(itemId);
            } catch (Exception e) {
                RuralRoutes.LOGGER.warn("Invalid item reference: {}", itemRef);
                return false;
            }
        }
    }

    /**
     * 获取引用下的所有物品
     * @param ref 引用字符串，如 "#minecraft:axes" 或 "minecraft:bread"
     * @return 不可变的物品集合，如果是精确物品则返回单元素集合
     */
    public static Set<Item> getItems(String ref) {
        return TAG_CACHE.computeIfAbsent(ref, TagLookupCache::resolveItems);
    }

    /**
     * 解析引用下的物品集合（内部方法，带缓存）
     */
    private static Set<Item> resolveItems(String ref) {
        if (isTagRef(ref)) {
            return resolveTagItems(ref);
        } else {
            return resolveSingleItem(ref);
        }
    }

    /**
     * 展开标签为物品集合
     */
    private static Set<Item> resolveTagItems(String tagRef) {
        String tagId = parseRefId(tagRef);

        try {
            ResourceLocation tagLocation = ResourceLocation.parse(tagId);
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagLocation);

            Set<Item> items = BuiltInRegistries.ITEM.getOrCreateTag(tagKey)
                .stream()
                .map(holder -> holder.value())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            if (items.isEmpty()) {
                RuralRoutes.LOGGER.warn("Tag reference resolved to empty set: {}", tagRef);
            }

            return Collections.unmodifiableSet(items);
        } catch (Exception e) {
            RuralRoutes.LOGGER.warn("Failed to resolve tag reference: {}", tagRef);
            return Collections.emptySet();
        }
    }

    /**
     * 解析精确物品引用
     */
    private static Set<Item> resolveSingleItem(String itemRef) {
        try {
            ResourceLocation itemId = ResourceLocation.parse(itemRef);
            Item item = BuiltInRegistries.ITEM.get(itemId);
            if (item == null) {
                RuralRoutes.LOGGER.warn("Item not found: {}", itemRef);
                return Collections.emptySet();
            }
            return Collections.singleton(item);
        } catch (Exception e) {
            RuralRoutes.LOGGER.warn("Invalid item reference: {}", itemRef);
            return Collections.emptySet();
        }
    }

    /**
     * 解析引用为 ResourceLocation（去除 # 前缀）
     * @param ref 引用字符串
     * @return ResourceLocation
     */
    public static ResourceLocation parseRef(String ref) {
        String id = parseRefId(ref);
        return ResourceLocation.parse(id);
    }

    /**
     * 获取引用 ID（去除 # 前缀）
     * @param ref 引用字符串
     * @return 不带前缀的 ID
     */
    public static String parseRefId(String ref) {
        return isTagRef(ref) ? ref.substring(1) : ref;
    }

    /**
     * 判断引用是否为标签引用
     * @param ref 引用字符串
     * @return 是否以 # 开头
     */
    public static boolean isTagRef(String ref) {
        return ref != null && ref.startsWith("#");
    }

    /**
     * 清空所有缓存
     * 应在 TagsUpdatedEvent 时调用
     */
    public static void invalidate() {
        int size = TAG_CACHE.size();
        TAG_CACHE.clear();
        if (size > 0) {
            RuralRoutes.LOGGER.debug("TagLookupCache invalidated, cleared {} entries", size);
        }
    }
}
