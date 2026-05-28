package github.mczme.ruralroutes.core.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 玩家商路图册数据。
 */
public final class TradeAtlasState {

    private final List<TradeAtlasNode> nodes;
    private boolean firstEntryUsed;
    private boolean locating;
    private Optional<UUID> currentTargetNodeId;

    public static TradeAtlasState empty() {
        return new TradeAtlasState(List.of(), false, false, Optional.empty());
    }

    public static final Codec<TradeAtlasState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        TradeAtlasNode.CODEC.listOf().fieldOf("nodes").forGetter(TradeAtlasState::nodes),
        Codec.BOOL.optionalFieldOf("first_entry_used", false).forGetter(TradeAtlasState::firstEntryUsed),
        Codec.BOOL.optionalFieldOf("locating", false).forGetter(TradeAtlasState::locating),
        UUIDUtil.CODEC.optionalFieldOf("current_target_node_id").forGetter(TradeAtlasState::currentTargetNodeId)
    ).apply(instance, TradeAtlasState::new));

    public TradeAtlasState(List<TradeAtlasNode> nodes, boolean firstEntryUsed, boolean locating,
            Optional<UUID> currentTargetNodeId) {
        this.nodes = new ArrayList<>(Objects.requireNonNull(nodes, "nodes"));
        this.firstEntryUsed = firstEntryUsed;
        this.locating = locating;
        this.currentTargetNodeId = currentTargetNodeId == null ? Optional.empty() : currentTargetNodeId;
    }

    public List<TradeAtlasNode> nodes() {
        return Collections.unmodifiableList(nodes);
    }

    public boolean firstEntryUsed() {
        return firstEntryUsed;
    }

    public boolean locating() {
        return locating;
    }

    public Optional<UUID> currentTargetNodeId() {
        return currentTargetNodeId;
    }

    public boolean hasNodes() {
        return !nodes.isEmpty();
    }

    public boolean hasClueNodes() {
        for (TradeAtlasNode node : nodes) {
            if (node.status() == AtlasNodeStatus.CLUE) {
                return true;
            }
        }
        return false;
    }

    public Optional<TradeAtlasNode> findNodeById(UUID nodeId) {
        if (nodeId == null) {
            return Optional.empty();
        }
        for (TradeAtlasNode node : nodes) {
            if (node.id().equals(nodeId)) {
                return Optional.of(node);
            }
        }
        return Optional.empty();
    }

    public Optional<TradeAtlasNode> findNodeAt(ResourceLocation dimensionId, BlockPos position) {
        for (TradeAtlasNode node : nodes) {
            if (node.sameLocation(dimensionId, position)) {
                return Optional.of(node);
            }
        }
        return Optional.empty();
    }

    public Optional<TradeAtlasNode> currentTarget() {
        return currentTargetNodeId.flatMap(this::findNodeById);
    }

    public void setFirstEntryUsed(boolean firstEntryUsed) {
        this.firstEntryUsed = firstEntryUsed;
    }

    public void setLocating(boolean locating) {
        this.locating = locating;
    }

    public void setCurrentTarget(UUID nodeId) {
        this.currentTargetNodeId = Optional.ofNullable(nodeId);
    }

    public void clearCurrentTarget() {
        this.currentTargetNodeId = Optional.empty();
    }

    public void setCurrentTargetIfAbsent(UUID nodeId) {
        if (currentTargetNodeId.isPresent() || nodeId == null) {
            return;
        }
        currentTargetNodeId = Optional.of(nodeId);
    }

    public boolean addClueNode(TradeAtlasNode node) {
        if (node == null) {
            return false;
        }

        Optional<TradeAtlasNode> existing = findNodeAt(node.dimensionId(), node.position());
        if (existing.isPresent()) {
            setCurrentTargetIfAbsent(existing.get().id());
            return false;
        }

        nodes.add(node);
        setCurrentTargetIfAbsent(node.id());
        return true;
    }

    public void upsertNodeAtLocation(TradeAtlasNode node, boolean selectIfEmpty) {
        if (node == null) {
            return;
        }

        int index = findNodeIndex(node.dimensionId(), node.position());
        if (index >= 0) {
            nodes.set(index, node);
        } else {
            nodes.add(node);
        }

        if (selectIfEmpty) {
            setCurrentTargetIfAbsent(node.id());
        }
    }

    private int findNodeIndex(ResourceLocation dimensionId, BlockPos position) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).sameLocation(dimensionId, position)) {
                return i;
            }
        }
        return -1;
    }
}
