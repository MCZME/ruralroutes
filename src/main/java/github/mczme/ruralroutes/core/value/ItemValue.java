package github.mczme.ruralroutes.core.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 物品基础价值数据
 * 通过 DataMaps 附加到物品上
 */
public record ItemValue(int value) {
    @SuppressWarnings("null")
    public static final Codec<ItemValue> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.INT.fieldOf("value").forGetter(ItemValue::value)
        ).apply(instance, ItemValue::new)
    );
}
