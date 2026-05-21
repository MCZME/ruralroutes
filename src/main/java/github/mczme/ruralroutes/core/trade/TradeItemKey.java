package github.mczme.ruralroutes.core.trade;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import github.mczme.ruralroutes.core.util.TagLookupCache;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 库存身份键。
 *
 * 承载“基础物品 + 组件签名”，供库存、显示和规则匹配共用。
 */
public record TradeItemKey(
    ResourceLocation itemId,
    Optional<Map<String, String>> componentSignature
) {
    public static final Codec<TradeItemKey> CODEC = Codec.STRING.flatXmap(
        TradeItemKey::fromCanonicalString,
        key -> DataResult.success(key.canonicalKey())
    );

    public TradeItemKey {
        itemId = Objects.requireNonNull(itemId, "itemId");
        componentSignature = componentSignature
            .map(Map::copyOf)
            .filter(signature -> !signature.isEmpty());
    }

    public static TradeItemKey of(ResourceLocation itemId) {
        return new TradeItemKey(itemId, Optional.empty());
    }

    public static TradeItemKey of(ResourceLocation itemId, Map<String, String> componentSignature) {
        return new TradeItemKey(itemId, Optional.of(componentSignature));
    }

    public static TradeItemKey from(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return new TradeItemKey(itemId, extractSignature(stack));
    }

    public static DataResult<TradeItemKey> fromCanonicalString(String raw) {
        if (raw == null || raw.isBlank()) {
            return DataResult.error(() -> "TradeItemKey cannot be blank");
        }

        String value = raw.strip();
        int separator = value.indexOf('|');
        if (separator < 0) {
            ResourceLocation itemId = ResourceLocation.tryParse(value);
            return itemId == null
                ? DataResult.error(() -> "Invalid item id: " + value)
                : DataResult.success(TradeItemKey.of(itemId));
        }

        String itemPart = value.substring(0, separator);
        String componentPart = value.substring(separator + 1);
        ResourceLocation itemId = ResourceLocation.tryParse(itemPart);
        if (itemId == null) {
            return DataResult.error(() -> "Invalid item id: " + itemPart);
        }
        if (componentPart.isBlank()) {
            return DataResult.success(TradeItemKey.of(itemId));
        }

        try {
            JsonElement parsed = JsonParser.parseString(componentPart);
            if (!parsed.isJsonObject()) {
                return DataResult.error(() -> "Invalid component signature for " + value);
            }

            Map<String, String> signature = parsed.getAsJsonObject().entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().getAsString(),
                    (left, right) -> left,
                    LinkedHashMap::new
                ));
            return DataResult.success(signature.isEmpty()
                ? TradeItemKey.of(itemId)
                : TradeItemKey.of(itemId, signature));
        } catch (Exception e) {
            return DataResult.error(() -> "Failed to parse TradeItemKey: " + e.getMessage());
        }
    }

    public boolean hasComponents() {
        return componentSignature.isPresent();
    }

    public boolean matchesExactItem(String expectedItemId, Map<String, String> expectedSignature) {
        if (!itemId.toString().equals(expectedItemId)) {
            return false;
        }
        return componentSignature.map(signature -> signature.equals(expectedSignature)).orElse(expectedSignature.isEmpty());
    }

    /**
     * 生成用于库存/规则匹配的稳定文本键。
     *
     * 使用 item id 作为前缀；当组件签名存在时，后缀使用 JSON 对象编码，便于往返解析。
     */
    public String canonicalKey() {
        if (componentSignature.isEmpty()) {
            return itemId.toString();
        }

        JsonObject json = new JsonObject();
        componentSignature.get().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> json.addProperty(entry.getKey(), entry.getValue()));
        return itemId + "|" + json;
    }

    public TradeTargetRef toTargetRef() {
        return componentSignature
            .map(signature -> TradeTargetRef.exactItem(itemId.toString(), signature))
            .orElseGet(() -> TradeTargetRef.item(itemId.toString()));
    }

    public boolean matchesTarget(TradeTargetRef targetRef) {
        if (targetRef == null) {
            return false;
        }
        if (targetRef.isItem()) {
            return matchesExactItem(targetRef.itemId().orElseThrow(), targetRef.components().orElse(Map.of()));
        }
        if (targetRef.isTag()) {
            return TagLookupCache.matchesItem(itemId, "#" + targetRef.tagId().orElseThrow());
        }
        return false;
    }

    public ItemStack asItemStack() {
        Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
        if (item == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(item);
        componentSignature.ifPresent(components -> applyComponents(stack, components));
        return stack;
    }

    private static Optional<Map<String, String>> extractSignature(ItemStack stack) {
        try {
            var patch = stack.getComponentsPatch();
            Map<String, String> signature = patch.entrySet().stream()
                .map(entry -> {
                    DataComponentType<?> type = entry.getKey();
                    ResourceLocation componentId = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
                    if (componentId == null) {
                        return null;
                    }

                    Object rawValue = entry.getValue();
                    if (rawValue instanceof Optional<?> optional) {
                        if (optional.isEmpty()) {
                            return null;
                        }
                        rawValue = optional.get();
                    }
                    if (rawValue == null) {
                        return null;
                    }

                    JsonElement encoded = encodeComponentValue(type, rawValue);
                    if (encoded == null) {
                        return null;
                    }
                    return Map.entry(componentId.toString(), encoded.toString());
                })
                .filter(Objects::nonNull)
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (left, right) -> left,
                    LinkedHashMap::new
                ));
            return signature.isEmpty() ? Optional.empty() : Optional.of(signature);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static JsonElement encodeComponentValue(DataComponentType type, Object value) {
        return (JsonElement) type.codec().encodeStart(JsonOps.INSTANCE, value)
            .result()
            .orElse(null);
    }

    private static void applyComponents(ItemStack stack, Map<String, String> components) {
        for (Map.Entry<String, String> entry : components.entrySet()) {
            ResourceLocation componentId = ResourceLocation.tryParse(entry.getKey());
            if (componentId == null) {
                continue;
            }

            DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getOptional(componentId).orElse(null);
            if (type == null) {
                continue;
            }

            try {
                JsonElement json = JsonParser.parseString(entry.getValue());
                Object value = type.codec().parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(err -> { })
                    .orElse(null);
                if (value != null) {
                    setComponent(stack, type, value);
                }
            } catch (Exception ignored) {
                // 保持 best-effort 还原，不让单个组件污染整个栈。
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setComponent(ItemStack stack, DataComponentType type, Object value) {
        stack.set(type, value);
    }
}
