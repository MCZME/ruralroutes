package github.mczme.ruralroutes.client.gui.component.atlas;

import github.mczme.ruralroutes.core.atlas.TradeAtlasNode;
import github.mczme.ruralroutes.core.atlas.TradeRoute;
import github.mczme.ruralroutes.core.atlas.TradeRouteSegment;
import github.mczme.ruralroutes.core.atlas.TradeRouteStop;
import github.mczme.ruralroutes.core.atlas.TradeAtlasState;

import java.util.Optional;
import java.util.UUID;

public final class TradeAtlasViewState {

    public enum DetailMode {
        NODE,
        ROUTE
    }

    private UUID selectedNodeId;
    private UUID selectedRouteId;
    private UUID selectedRouteStopId;
    private UUID selectedRouteSegmentId;
    private UUID routeDraftStartNodeId;
    private DetailMode detailMode = DetailMode.NODE;
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

    public Optional<TradeRoute> selectedRoute(TradeAtlasState state) {
        Optional<TradeRoute> selected = state.findRouteById(selectedRouteId);
        if (selected.isPresent()) {
            return selected;
        }
        if (!state.routes().isEmpty()) {
            selectedRouteId = state.routes().get(0).id();
            return Optional.of(state.routes().get(0));
        }
        selectedRouteId = null;
        selectedRouteStopId = null;
        selectedRouteSegmentId = null;
        return Optional.empty();
    }

    public Optional<TradeRouteStop> selectedRouteStop(TradeAtlasState state) {
        return selectedRoute(state).flatMap(route -> route.findStop(selectedRouteStopId)
            .or(() -> route.stops().isEmpty() ? Optional.empty() : Optional.of(route.stops().get(route.stops().size() - 1))));
    }

    public Optional<TradeRouteSegment> selectedRouteSegment(TradeAtlasState state) {
        return selectedRoute(state).flatMap(route -> route.findSegment(selectedRouteSegmentId)
            .or(() -> route.segments().isEmpty() ? Optional.empty() : Optional.of(route.segments().get(0))));
    }

    public void selectNode(TradeAtlasNode node) {
        selectedNodeId = node == null ? null : node.id();
        detailMode = DetailMode.NODE;
    }

    public boolean isSelected(UUID nodeId) {
        return selectedNodeId != null && selectedNodeId.equals(nodeId);
    }

    public void selectRoute(TradeRoute route) {
        selectedRouteId = route == null ? null : route.id();
        detailMode = DetailMode.ROUTE;
        if (route != null) {
            if (selectedRouteStopId == null || route.findStop(selectedRouteStopId).isEmpty()) {
                selectedRouteStopId = route.stops().isEmpty() ? null : route.stops().get(0).id();
            }
            if (selectedRouteSegmentId == null || route.findSegment(selectedRouteSegmentId).isEmpty()) {
                selectedRouteSegmentId = route.segments().isEmpty() ? null : route.segments().get(0).id();
            }
        }
    }

    public void selectRouteStop(TradeRoute route, TradeRouteStop stop) {
        selectedRouteId = route == null ? null : route.id();
        selectedRouteStopId = stop == null ? null : stop.id();
        detailMode = DetailMode.ROUTE;
    }

    public void selectRouteSegment(TradeRoute route, TradeRouteSegment segment) {
        selectedRouteId = route == null ? null : route.id();
        selectedRouteSegmentId = segment == null ? null : segment.id();
        detailMode = DetailMode.ROUTE;
    }

    public boolean isRouteSelected(UUID routeId) {
        return selectedRouteId != null && selectedRouteId.equals(routeId);
    }

    public boolean isRouteStopSelected(UUID stopId) {
        return selectedRouteStopId != null && selectedRouteStopId.equals(stopId);
    }

    public boolean isRouteSegmentSelected(UUID segmentId) {
        return selectedRouteSegmentId != null && selectedRouteSegmentId.equals(segmentId);
    }

    public Optional<UUID> routeDraftStartNodeId() {
        return Optional.ofNullable(routeDraftStartNodeId);
    }

    public void setRouteDraftStartNode(TradeAtlasNode node) {
        routeDraftStartNodeId = node == null ? null : node.id();
    }

    public void clearRouteDraftStartNode() {
        routeDraftStartNodeId = null;
    }

    public DetailMode detailMode() {
        return detailMode;
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
