package github.mczme.ruralroutes.core.atlas;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.network.packet.TradeAtlasActionPayload;
import github.mczme.ruralroutes.core.theme.VillageStyle;
import github.mczme.ruralroutes.network.packet.OpenTradeAtlasPayload;
import github.mczme.ruralroutes.network.packet.TradeRouteActionPayload;
import github.mczme.ruralroutes.register.RRAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 商路图册服务端骨架。
 */
public final class TradeAtlasManager {

    private static final int STRUCTURE_SEARCH_RADIUS = 100;
    private static final int[] ROUTE_COLORS = {
        0xFF9BD2F7,
        0xFFE5BE73,
        0xFFA9DD82,
        0xFFF08B7C,
        0xFFC8A2FF,
        0xFF7DD6C5
    };

    private TradeAtlasManager() {
    }

    public static TradeAtlasState getState(Player player) {
        return player.getData(RRAttachments.PLAYER_ATLAS.get());
    }

    public static void setState(Player player, TradeAtlasState state) {
        player.setData(RRAttachments.PLAYER_ATLAS.get(), state);
    }

    public static void openAtlas(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new OpenTradeAtlasPayload(getState(player)));
    }

    public static void handleAtlasAction(ServerPlayer player, TradeAtlasActionPayload.Action action,
            @Nullable VillageStyle style, @Nullable UUID nodeId) {
        switch (action) {
            case REQUEST_LOCATE -> handleLocateRequest(player, style);
            case CANCEL_PENDING_CLUE -> handleCancelPendingClue(player);
            case SET_TARGET -> handleSetTarget(player, nodeId);
            case CLEAR_TARGET -> handleClearTarget(player);
        }

        openAtlas(player);
    }

    public static void handleRouteAction(ServerPlayer player, TradeRouteActionPayload payload) {
        TradeAtlasState state = getState(player);
        boolean changed = switch (payload.action()) {
            case CREATE_ROUTE -> handleCreateRoute(state, payload.nodeId(), payload.stopId());
            case DELETE_ROUTE -> state.removeRoute(payload.routeId());
            case TOGGLE_ROUTE_VISIBLE -> handleToggleRouteVisible(state, payload.routeId());
            case CYCLE_ROUTE_STATUS -> handleCycleRouteStatus(state, payload.routeId());
            case RENAME_ROUTE -> handleRenameRoute(state, payload.routeId(), payload.text());
            case ADD_STOP -> handleAddStop(state, payload.routeId(), payload.nodeId(), payload.stopId());
            case REMOVE_STOP -> handleRemoveStop(state, payload.routeId(), payload.stopId());
            case UPDATE_STOP_ROLE -> handleUpdateStopRole(state, payload.routeId(), payload.stopId(), payload.text());
            case UPDATE_STOP_NOTE -> handleUpdateStopNote(state, payload.routeId(), payload.stopId(), payload.text());
            case CYCLE_SEGMENT_DIRECTION -> handleCycleSegmentDirection(state, payload.routeId(), payload.segmentId());
        };

        if (changed) {
            setState(player, state);
        }
        openAtlas(player);
    }

    public static boolean canRequestLocate(TradeAtlasState state) {
        return state != null && !state.locating() && !state.hasPendingClue();
    }

    public static void recordTradeStationVisit(ServerPlayer player, BlockPos stationPos, VillageStyle style,
            @Nullable ResourceLocation themeName, boolean validVillage) {
        ServerLevel level = player.serverLevel();
        ResourceLocation dimensionId = level.dimension().location();
        TradeAtlasState state = getState(player);
        // 结构线索位置通常不是贸易站方块位置；交互贸易站时优先把最近的同风格线索升级。
        TradeAtlasNode existing = state.findNodeAt(dimensionId, stationPos)
            .or(() -> findNearestClueByStyle(state, dimensionId, style, stationPos))
            .orElse(null);

        TradeAtlasNode updatedNode;
        if (validVillage) {
            if (existing != null) {
                updatedNode = existing
                    .withStyle(style)
                    .withStatus(AtlasNodeStatus.RECORDED)
                    .withThemeName(themeName);
            } else {
                updatedNode = TradeAtlasNode.recorded(
                    UUID.randomUUID(),
                    dimensionId,
                    stationPos,
                    style,
                    themeName
                );
            }
            state.upsertNodeAtLocation(updatedNode, true);
        } else {
            if (existing != null) {
                updatedNode = existing
                    .withStyle(style)
                    .withStatus(AtlasNodeStatus.INVALID)
                    .withThemeName(themeName);
            } else {
                updatedNode = TradeAtlasNode.invalid(
                    UUID.randomUUID(),
                    dimensionId,
                    stationPos,
                    style,
                    themeName
                );
            }
            state.upsertNodeAtLocation(updatedNode, false);
            player.displayClientMessage(
                Component.translatable("block.ruralroutes.trade_station.invalid_village"),
                true
            );
        }

        setState(player, state);
    }

    private static void handleLocateRequest(ServerPlayer player, @Nullable VillageStyle style) {
        if (style == null) {
            player.displayClientMessage(
                Component.translatable("gui.ruralroutes.trade_atlas.locate.no_style"),
                true
            );
            return;
        }

        TradeAtlasState currentState = getState(player);
        if (currentState.locating()) {
            player.displayClientMessage(
                Component.translatable("gui.ruralroutes.trade_atlas.locate.busy"),
                true
            );
            return;
        }
        if (currentState.hasPendingClue()) {
            player.displayClientMessage(
                Component.translatable("gui.ruralroutes.trade_atlas.locate.pending_clue"),
                true
            );
            return;
        }

        boolean firstEntryFlow = !currentState.firstEntryUsed() && currentState.nodes().isEmpty();
        currentState.setLocating(true);
        setState(player, currentState);

        Component feedback = null;

        try {
            feedback = locateOnce(player, currentState, style, firstEntryFlow);
        } catch (Exception exception) {
            RuralRoutes.LOGGER.error("Failed to locate village for atlas", exception);
            feedback = Component.translatable("gui.ruralroutes.trade_atlas.locate.failed");
        } finally {
            currentState.setLocating(false);
            setState(player, currentState);
        }

        if (feedback != null) {
            player.displayClientMessage(feedback, true);
        }
    }

    private static Component locateOnce(ServerPlayer player, TradeAtlasState currentState, VillageStyle style,
            boolean firstEntryFlow) {
        ServerLevel level = player.serverLevel();
        BlockPos origin = player.blockPosition();
        ResourceLocation dimensionId = level.dimension().location();
        AtlasVillageStructureCache cache = AtlasVillageStructureCache.get(level);

        // 服务器已经定位过的结构可以直接复用，玩家图册状态只负责过滤个人已知线索。
        AtlasVillageStructureCache.Entry cachedEntry = cache.findNearest(
            dimensionId,
            style,
            origin,
            entry -> currentState.findNodeAt(entry.dimensionId(), entry.locatePos()).isEmpty()
        ).orElse(null);
        if (cachedEntry != null) {
            return addLocatedClue(currentState, style, cachedEntry.dimensionId(), cachedEntry.locatePos(), firstEntryFlow);
        }

        // 缓存没有可用候选时，再执行一次专用结构搜索，并跳过所有 level 已缓存结构。
        AtlasVillageLocator.LocatedVillageStructure located = AtlasVillageLocator.findNearestUncached(
            level,
            style,
            origin,
            STRUCTURE_SEARCH_RADIUS,
            candidate -> cache.contains(
                candidate.dimensionId(),
                candidate.style(),
                candidate.structureId(),
                candidate.structureChunk()
            )
        ).orElse(null);

        if (located == null) {
            return Component.translatable("gui.ruralroutes.trade_atlas.locate.failed");
        }

        AtlasVillageStructureCache.Entry remembered = cache.remember(
            located.dimensionId(),
            located.style(),
            located.structureId(),
            located.structureChunk(),
            located.locatePos()
        );

        return addLocatedClue(currentState, style, remembered.dimensionId(), remembered.locatePos(), firstEntryFlow);
    }

    /**
     * 将定位结果写入玩家图册。
     *
     * 首次免费资格只在真正新增线索后消耗，避免搜索失败或重复候选吃掉入口机会。
     */
    private static Component addLocatedClue(TradeAtlasState currentState, VillageStyle style,
            ResourceLocation dimensionId, BlockPos foundPos, boolean firstEntryFlow) {
        TradeAtlasNode existing = currentState.findNodeAt(dimensionId, foundPos).orElse(null);
        if (existing != null) {
            currentState.setCurrentTargetIfAbsent(existing.id());
            return Component.translatable("gui.ruralroutes.trade_atlas.locate.duplicate");
        }

        TradeAtlasNode node = TradeAtlasNode.clue(dimensionId, foundPos, style);
        boolean added = currentState.addClueNode(node);
        if (!added) {
            return Component.translatable("gui.ruralroutes.trade_atlas.locate.duplicate");
        }
        if (firstEntryFlow) {
            currentState.setFirstEntryUsed(true);
        }
        return Component.translatable(
                "gui.ruralroutes.trade_atlas.locate.success",
                Component.translatable(style.translationKey())
            );
    }

    private static void handleSetTarget(ServerPlayer player, @Nullable UUID nodeId) {
        if (nodeId == null) {
            return;
        }

        TradeAtlasState state = getState(player);
        if (state.findNodeById(nodeId).isEmpty()) {
            return;
        }

        state.setCurrentTarget(nodeId);
        setState(player, state);
    }

    private static void handleCancelPendingClue(ServerPlayer player) {
        TradeAtlasState state = getState(player);
        if (!state.hasPendingClue()) {
            return;
        }

        state.clearPendingClue();
        setState(player, state);
        player.displayClientMessage(
            Component.translatable("gui.ruralroutes.trade_atlas.locate.cancelled"),
            true
        );
    }

    private static void handleClearTarget(ServerPlayer player) {
        TradeAtlasState state = getState(player);
        state.clearCurrentTarget();
        setState(player, state);
    }

    private static boolean handleCreateRoute(TradeAtlasState state, @Nullable UUID firstNodeId,
            @Nullable UUID secondNodeId) {
        if (firstNodeId == null || secondNodeId == null) {
            return false;
        }
        if (state.findNodeById(firstNodeId).isEmpty() || state.findNodeById(secondNodeId).isEmpty()) {
            return false;
        }

        int routeNumber = state.routes().size() + 1;
        int color = ROUTE_COLORS[state.routes().size() % ROUTE_COLORS.length];
        TradeRoute route = TradeRoute.create("Route " + routeNumber, color, firstNodeId, secondNodeId);
        return state.addRoute(route, false);
    }

    private static boolean handleToggleRouteVisible(TradeAtlasState state, @Nullable UUID routeId) {
        if (routeId == null || state.findRouteById(routeId).isEmpty()) {
            return false;
        }
        state.toggleRouteVisible(routeId);
        return true;
    }

    private static boolean handleCycleRouteStatus(TradeAtlasState state, @Nullable UUID routeId) {
        TradeRoute route = state.findRouteById(routeId).orElse(null);
        if (route == null) {
            return false;
        }
        return state.replaceRoute(route.withStatus(route.status().next()));
    }

    private static boolean handleRenameRoute(TradeAtlasState state, @Nullable UUID routeId, String text) {
        TradeRoute route = state.findRouteById(routeId).orElse(null);
        if (route == null) {
            return false;
        }
        return state.replaceRoute(route.withName(text));
    }

    private static boolean handleAddStop(TradeAtlasState state, @Nullable UUID routeId, @Nullable UUID nodeId,
            @Nullable UUID anchorStopId) {
        TradeRoute route = state.findRouteById(routeId).orElse(null);
        if (route == null || nodeId == null || state.findNodeById(nodeId).isEmpty()) {
            return false;
        }
        return state.replaceRoute(route.withAddedStop(nodeId, anchorStopId));
    }

    private static boolean handleRemoveStop(TradeAtlasState state, @Nullable UUID routeId, @Nullable UUID stopId) {
        TradeRoute route = state.findRouteById(routeId).orElse(null);
        if (route == null || stopId == null) {
            return false;
        }
        TradeRoute updated = route.withoutStop(stopId);
        if (updated == route) {
            return false;
        }
        return state.replaceRoute(updated);
    }

    private static boolean handleUpdateStopRole(TradeAtlasState state, @Nullable UUID routeId,
            @Nullable UUID stopId, String text) {
        TradeRoute route = state.findRouteById(routeId).orElse(null);
        if (route == null || stopId == null) {
            return false;
        }
        TradeRouteStop stop = route.findStop(stopId).orElse(null);
        if (stop == null) {
            return false;
        }
        return state.replaceRoute(route.withUpdatedStop(stop.withRole(text)));
    }

    private static boolean handleUpdateStopNote(TradeAtlasState state, @Nullable UUID routeId,
            @Nullable UUID stopId, String text) {
        TradeRoute route = state.findRouteById(routeId).orElse(null);
        if (route == null || stopId == null) {
            return false;
        }
        TradeRouteStop stop = route.findStop(stopId).orElse(null);
        if (stop == null) {
            return false;
        }
        return state.replaceRoute(route.withUpdatedStop(stop.withNote(text)));
    }

    private static boolean handleCycleSegmentDirection(TradeAtlasState state, @Nullable UUID routeId,
            @Nullable UUID segmentId) {
        TradeRoute route = state.findRouteById(routeId).orElse(null);
        if (route == null || segmentId == null) {
            return false;
        }
        TradeRouteSegment segment = route.findSegment(segmentId).orElse(null);
        if (segment == null) {
            return false;
        }
        return state.replaceRoute(route.withUpdatedSegment(segment.withDirection(segment.direction().next())));
    }

    private static java.util.Optional<TradeAtlasNode> findNearestClueByStyle(TradeAtlasState state,
            ResourceLocation dimensionId, VillageStyle style, BlockPos anchorPos) {
        return state.nodes().stream()
            .filter(node -> node.dimensionId().equals(dimensionId))
            .filter(node -> node.style() == style)
            .filter(node -> node.status() == AtlasNodeStatus.CLUE)
            .min(java.util.Comparator.comparingDouble(node -> node.position().distSqr(anchorPos)));
    }
}
