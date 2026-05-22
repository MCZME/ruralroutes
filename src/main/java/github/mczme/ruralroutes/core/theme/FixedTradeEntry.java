package github.mczme.ruralroutes.core.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * 固定交换契约条目
 */
public record FixedTradeEntry(
    List<InputEntry> inputs,
    List<OutputEntry> outputs
) implements TradeContractEntry {

    public static final MapCodec<FixedTradeEntry> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            InputEntry.CODEC.listOf().fieldOf("inputs").forGetter(FixedTradeEntry::inputs),
            OutputEntry.CODEC.listOf().fieldOf("outputs").forGetter(FixedTradeEntry::outputs)
        ).apply(instance, FixedTradeEntry::new)
    );

    public static final Codec<FixedTradeEntry> CODEC = MAP_CODEC.codec();

    @Override
    public String typeString() {
        return "fixed";
    }
}
