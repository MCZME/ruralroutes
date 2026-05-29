package github.mczme.ruralroutes.client.gui.component.atlas;

import github.mczme.ruralroutes.core.atlas.TradeAtlasNode;
import github.mczme.ruralroutes.core.atlas.TradeRoute;
import github.mczme.ruralroutes.core.atlas.TradeRouteSegment;
import github.mczme.ruralroutes.core.atlas.TradeRouteStop;
import github.mczme.ruralroutes.core.atlas.TradeAtlasState;

import java.util.Optional;
import java.util.UUID;

public final class TradeAtlasViewState {

    public enum SelectionType {
        NONE,
        NODE,
        ROUTE,
        ROUTE_STOP,
        ROUTE_SEGMENT
    }

    private SelectionType selectionType = SelectionType.NONE;
    private UUID selectedNodeId;
    private UUID selectedRouteId;
    private UUID selectedRouteStopId;
    private UUID selectedRouteSegmentId;
    private UUID routeDraftStartNodeId;
    private boolean locateSelectionOpen;

    public TradeAtlasViewState(TradeAtlasState state) {
    }

    public Optional<TradeAtlasNode> selectedNode(TradeAtlasState state) {
        if (selectionType != SelectionType.NODE) {
            return Optional.empty();
        }
        return state.findNodeById(selectedNodeId);
    }

    public Optional<TradeRoute> selectedRoute(TradeAtlasState state) {
        if (selectionType != SelectionType.ROUTE
                && selectionType != SelectionType.ROUTE_STOP
                && selectionType != SelectionType.ROUTE_SEGMENT) {
            return Optional.empty();
        }
        return state.findRouteById(selectedRouteId);
    }

    public Optional<TradeRouteStop> selectedRouteStop(TradeAtlasState state) {
        if (selectionType != SelectionType.ROUTE_STOP) {
            return Optional.empty();
        }
        return selectedRoute(state).flatMap(route -> route.findStop(selectedRouteStopId));
    }

    public Optional<TradeRouteSegment> selectedRouteSegment(TradeAtlasState state) {
        if (selectionType != SelectionType.ROUTE_SEGMENT) {
            return Optional.empty();
        }
        return selectedRoute(state).flatMap(route -> route.findSegment(selectedRouteSegmentId));
    }

    public void selectNode(TradeAtlasNode node) {
        if (node == null) {
            clearSelection();
            return;
        }
        selectionType = SelectionType.NODE;
        selectedNodeId = node.id();
        selectedRouteId = null;
        selectedRouteStopId = null;
        selectedRouteSegmentId = null;
    }

    public boolean isNodeSelected(UUID nodeId) {
        return selectionType == SelectionType.NODE && selectedNodeId != null && selectedNodeId.equals(nodeId);
    }

    public boolean isSelected(UUID nodeId) {
        return isNodeSelected(nodeId);
    }

    public void selectRoute(TradeRoute route) {
        if (route == null) {
            clearSelection();
            return;
        }
        selectionType = SelectionType.ROUTE;
        selectedNodeId = null;
        selectedRouteId = route.id();
        selectedRouteStopId = null;
        selectedRouteSegmentId = null;
    }

    public void selectRouteStop(TradeRoute route, TradeRouteStop stop) {
        if (route == null || stop == null) {
            clearSelection();
            return;
        }
        selectionType = SelectionType.ROUTE_STOP;
        selectedNodeId = null;
        selectedRouteId = route.id();
        selectedRouteStopId = stop.id();
        selectedRouteSegmentId = null;
    }

    public void selectRouteSegment(TradeRoute route, TradeRouteSegment segment) {
        if (route == null || segment == null) {
            clearSelection();
            return;
        }
        selectionType = SelectionType.ROUTE_SEGMENT;
        selectedNodeId = null;
        selectedRouteId = route.id();
        selectedRouteStopId = null;
        selectedRouteSegmentId = segment.id();
    }

    public boolean isRouteSelected(UUID routeId) {
        return selectionType == SelectionType.ROUTE && selectedRouteId != null && selectedRouteId.equals(routeId);
    }

    public boolean isRouteStopSelected(UUID stopId) {
        return selectionType == SelectionType.ROUTE_STOP && selectedRouteStopId != null && selectedRouteStopId.equals(stopId);
    }

    public boolean isRouteSegmentSelected(UUID segmentId) {
        return selectionType == SelectionType.ROUTE_SEGMENT && selectedRouteSegmentId != null
            && selectedRouteSegmentId.equals(segmentId);
    }

    public void clearSelection() {
        selectionType = SelectionType.NONE;
        selectedNodeId = null;
        selectedRouteId = null;
        selectedRouteStopId = null;
        selectedRouteSegmentId = null;
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

    public SelectionType selectionType() {
        return selectionType;
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
