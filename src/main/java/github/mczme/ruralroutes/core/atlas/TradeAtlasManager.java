package github.mczme.ruralroutes.core.atlas;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.network.packet.TradeAtlasActionPayload;
import github.mczme.ruralroutes.core.theme.VillageStyle;
import github.mczme.ruralroutes.network.packet.OpenTradeAtlasPayload;
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

    private static final int STRUCTURE_SEARCH_RADIUS = 1000;

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
            case SET_TARGET -> handleSetTarget(player, nodeId);
            case CLEAR_TARGET -> handleClearTarget(player);
        }

        openAtlas(player);
    }

    public static boolean canRequestLocate(TradeAtlasState state) {
        return state != null && !state.locating() && !state.hasClueNodes();
    }

    public static void recordTradeStationVisit(ServerPlayer player, BlockPos stationPos, VillageStyle style,
            @Nullable ResourceLocation themeName, boolean validVillage) {
        ServerLevel level = player.serverLevel();
        ResourceLocation dimensionId = level.dimension().location();
        TradeAtlasState state = getState(player);
        TradeAtlasNode existing = state.findNodeAt(dimensionId, stationPos).orElse(null);

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
        if (currentState.hasClueNodes()) {
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
        BlockPos foundPos = level.findNearestMapStructure(
            style.structureTag(),
            player.blockPosition(),
            STRUCTURE_SEARCH_RADIUS,
            false
        );

        if (foundPos == null) {
            return Component.translatable("gui.ruralroutes.trade_atlas.locate.failed");
        }

        ResourceLocation dimensionId = level.dimension().location();
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

    private static void handleClearTarget(ServerPlayer player) {
        TradeAtlasState state = getState(player);
        state.clearCurrentTarget();
        setState(player, state);
    }
}
