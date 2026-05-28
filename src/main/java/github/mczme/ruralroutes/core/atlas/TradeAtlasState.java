package github.mczme.ruralroutes.core.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.core.theme.VillageStyle;
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
    private Optional<UUID> pendingClueNodeId;
    private Optional<UUID> currentTargetNodeId;

    public static TradeAtlasState empty() {
        return new TradeAtlasState(List.of(), false, false, Optional.empty(), Optional.empty());
    }

    public static final Codec<TradeAtlasState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        TradeAtlasNode.CODEC.listOf().fieldOf("nodes").forGetter(TradeAtlasState::nodes),
        Codec.BOOL.optionalFieldOf("first_entry_used", false).forGetter(TradeAtlasState::firstEntryUsed),
        Codec.BOOL.optionalFieldOf("locating", false).forGetter(TradeAtlasState::locating),
        UUIDUtil.CODEC.optionalFieldOf("pending_clue_node_id").forGetter(TradeAtlasState::pendingClueNodeId),
        UUIDUtil.CODEC.optionalFieldOf("current_target_node_id").forGetter(TradeAtlasState::currentTargetNodeId)
    ).apply(instance, TradeAtlasState::new));

    public TradeAtlasState(List<TradeAtlasNode> nodes, boolean firstEntryUsed, boolean locating,
            Optional<UUID> pendingClueNodeId, Optional<UUID> currentTargetNodeId) {
        this.nodes = new ArrayList<>(Objects.requireNonNull(nodes, "nodes"));
        this.firstEntryUsed = firstEntryUsed;
        this.locating = locating;
        this.pendingClueNodeId = pendingClueNodeId == null ? Optional.empty() : pendingClueNodeId;
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

    public Optional<UUID> pendingClueNodeId() {
        return pendingClueNodeId;
    }

    public Optional<UUID> currentTargetNodeId() {
        return currentTargetNodeId;
    }

    public boolean hasNodes() {
        return !nodes.isEmpty();
    }

    public boolean hasPendingClue() {
        return pendingClueNodeId.flatMap(this::findNodeById)
            .map(node -> node.status() == AtlasNodeStatus.CLUE)
            .orElse(false);
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

    /**
     * 查找同维度、同风格的未确认线索。
     *
     * 图册结构定位拿到的是结构 locatePos；玩家实际交互的是贸易站方块位置。
     * 因此贸易站确认时需要按风格合并线索，而不是只按精确坐标匹配。
     */
    public Optional<TradeAtlasNode> findClueByStyle(ResourceLocation dimensionId, VillageStyle style) {
        for (TradeAtlasNode node : nodes) {
            if (node.dimensionId().equals(dimensionId)
                && node.style() == style
                && node.status() == AtlasNodeStatus.CLUE) {
                return Optional.of(node);
            }
        }
        return Optional.empty();
    }

    public Optional<TradeAtlasNode> currentTarget() {
        return currentTargetNodeId.flatMap(this::findNodeById);
    }

    public Optional<TradeAtlasNode> pendingClue() {
        return pendingClueNodeId.flatMap(this::findNodeById);
    }

    public void setFirstEntryUsed(boolean firstEntryUsed) {
        this.firstEntryUsed = firstEntryUsed;
    }

    public void setLocating(boolean locating) {
        this.locating = locating;
    }

    public void setPendingClueNodeId(UUID nodeId) {
        this.pendingClueNodeId = Optional.ofNullable(nodeId);
    }

    public void clearPendingClue() {
        this.pendingClueNodeId = Optional.empty();
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
        if (node.status() == AtlasNodeStatus.CLUE) {
            setPendingClueNodeId(node.id());
        }
        setCurrentTargetIfAbsent(node.id());
        return true;
    }

    /**
     * 新增或替换一个节点。
     *
     * 优先按节点 id 替换，允许结构线索在确认贸易站后移动到真实方块位置。
     * 如果没有同 id 节点，再退回到旧的“同位置合并”规则。
     */
    public void upsertNodeAtLocation(TradeAtlasNode node, boolean selectIfEmpty) {
        if (node == null) {
            return;
        }

        int index = findNodeIndex(node.id());
        if (index < 0) {
            index = findNodeIndex(node.dimensionId(), node.position());
        }
        if (index >= 0) {
            nodes.set(index, node);
        } else {
            nodes.add(node);
        }

        if (pendingClueNodeId.isPresent() && pendingClueNodeId.get().equals(node.id())
            && node.status() != AtlasNodeStatus.CLUE) {
            clearPendingClue();
        }

        if (selectIfEmpty) {
            setCurrentTargetIfAbsent(node.id());
        }
    }

    private int findNodeIndex(ResourceLocation dimensionId, BlockPos position) {
        for (int i = 0; i < nodes.size(); i++) {
            TradeAtlasNode node = nodes.get(i);
            if (node.sameLocation(dimensionId, position)) {
                return i;
            }
        }
        return -1;
    }

    private int findNodeIndex(UUID nodeId) {
        if (nodeId == null) {
            return -1;
        }
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).id().equals(nodeId)) {
                return i;
            }
        }
        return -1;
    }
}
