package github.mczme.ruralroutes.core.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.core.theme.VillageStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 商路图册中的一个村庄节点。
 */
public record TradeAtlasNode(
    UUID id,
    ResourceLocation dimensionId,
    BlockPos position,
    VillageStyle style,
    AtlasNodeStatus status,
    Optional<ResourceLocation> themeName
) {

    public static final Codec<TradeAtlasNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("id").forGetter(TradeAtlasNode::id),
        ResourceLocation.CODEC.fieldOf("dimension_id").forGetter(TradeAtlasNode::dimensionId),
        BlockPos.CODEC.fieldOf("position").forGetter(TradeAtlasNode::position),
        VillageStyle.CODEC.fieldOf("style").forGetter(TradeAtlasNode::style),
        AtlasNodeStatus.CODEC.fieldOf("status").forGetter(TradeAtlasNode::status),
        ResourceLocation.CODEC.optionalFieldOf("theme_name").forGetter(TradeAtlasNode::themeName)
    ).apply(instance, TradeAtlasNode::new));

    public TradeAtlasNode {
        id = Objects.requireNonNull(id, "id");
        dimensionId = Objects.requireNonNull(dimensionId, "dimensionId");
        position = Objects.requireNonNull(position, "position");
        style = Objects.requireNonNull(style, "style");
        status = Objects.requireNonNull(status, "status");
        themeName = themeName == null ? Optional.empty() : themeName;
    }

    public static TradeAtlasNode clue(ResourceLocation dimensionId, BlockPos position, VillageStyle style) {
        return new TradeAtlasNode(
            UUID.randomUUID(),
            dimensionId,
            position,
            style,
            AtlasNodeStatus.CLUE,
            Optional.empty()
        );
    }

    public static TradeAtlasNode recorded(UUID id, ResourceLocation dimensionId, BlockPos position,
            VillageStyle style, @Nullable ResourceLocation themeName) {
        return new TradeAtlasNode(
            id,
            dimensionId,
            position,
            style,
            AtlasNodeStatus.RECORDED,
            Optional.ofNullable(themeName)
        );
    }

    public static TradeAtlasNode invalid(UUID id, ResourceLocation dimensionId, BlockPos position,
            VillageStyle style, @Nullable ResourceLocation themeName) {
        return new TradeAtlasNode(
            id,
            dimensionId,
            position,
            style,
            AtlasNodeStatus.INVALID,
            Optional.ofNullable(themeName)
        );
    }

    public TradeAtlasNode withStatus(AtlasNodeStatus newStatus) {
        return new TradeAtlasNode(id, dimensionId, position, style, newStatus, themeName);
    }

    public TradeAtlasNode withStyle(VillageStyle newStyle) {
        return new TradeAtlasNode(id, dimensionId, position, newStyle, status, themeName);
    }

    public TradeAtlasNode withThemeName(@Nullable ResourceLocation newThemeName) {
        return new TradeAtlasNode(id, dimensionId, position, style, status, Optional.ofNullable(newThemeName));
    }

    public boolean sameLocation(ResourceLocation otherDimensionId, BlockPos otherPosition) {
        return dimensionId.equals(otherDimensionId) && position.equals(otherPosition);
    }
}
