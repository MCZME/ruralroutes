package github.mczme.ruralroutes.client.gui.component.atlas;

import github.mczme.ruralroutes.core.atlas.TradeAtlasNode;
import github.mczme.ruralroutes.core.atlas.TradeAtlasState;

import java.util.Optional;
import java.util.UUID;

public final class TradeAtlasViewState {

    private UUID selectedNodeId;
    private boolean locateSelectionOpen;

    public TradeAtlasViewState(TradeAtlasState state) {
        if (state.currentTargetNodeId().isPresent()) {
            selectedNodeId = state.currentTargetNodeId().get();
        } else if (!state.nodes().isEmpty()) {
            selectedNodeId = state.nodes().get(0).id();
        }
    }

    public Optional<TradeAtlasNode> selectedNode(TradeAtlasState state) {
        Optional<TradeAtlasNode> selected = state.findNodeById(selectedNodeId);
        if (selected.isPresent()) {
            return selected;
        }
        if (!state.nodes().isEmpty()) {
            selectedNodeId = state.nodes().get(0).id();
            return Optional.of(state.nodes().get(0));
        }
        selectedNodeId = null;
        return Optional.empty();
    }

    public void selectNode(TradeAtlasNode node) {
        selectedNodeId = node == null ? null : node.id();
    }

    public boolean isSelected(UUID nodeId) {
        return selectedNodeId != null && selectedNodeId.equals(nodeId);
    }

    public boolean isLocateSelectionOpen() {
        return locateSelectionOpen;
    }

    public void toggleLocateSelection() {
        locateSelectionOpen = !locateSelectionOpen;
    }

    public void closeLocateSelection() {
        locateSelectionOpen = false;
    }
}
