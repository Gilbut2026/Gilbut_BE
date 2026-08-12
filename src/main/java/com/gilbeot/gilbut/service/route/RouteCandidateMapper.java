package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteMetrics;
import com.gilbeot.gilbut.dto.route.RouteWalkSegment;
import com.gilbeot.gilbut.client.ai.dto.scoring.type.SegmentScope;
import com.gilbeot.gilbut.dto.route.transit.response.TransitLegResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRoutePointResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitWalkingStepResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteItemResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteSummaryResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteItemResponse;
import com.gilbeot.gilbut.dto.route.walking.response.RoutePointResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteSummaryResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingStepResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RouteCandidateMapper {

    public List<RouteCandidate> fromWalkingRoutes(
            WalkingRouteResponse response
    ) {
        if (response == null
                || response.getRoutes() == null
                || response.getRoutes().isEmpty()) {
            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }

        List<RouteCandidate> candidates =
                new ArrayList<>();

        for (int index = 0;
             index < response.getRoutes().size();
             index++) {
            candidates.add(
                    fromWalkingRoute(
                            response.getRoutes().get(index),
                            index + 1
                    )
            );
        }

        return candidates;
    }

    private RouteCandidate fromWalkingRoute(
            WalkingRouteItemResponse route,
            int providerRank
    ) {
        if (route == null
                || route.getRouteId() == null
                || route.getRouteOption() == null
                || route.getSummary() == null) {
            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }

        WalkingRouteSummaryResponse summary =
                route.getSummary();

        if (summary.getTotalTimeSec() == null
                || summary.getTotalDistanceM() == null) {
            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }

        return RouteCandidate.builder()
                .routeId(route.getRouteId())
                .routeType(RouteType.WALKING)
                .routeOption(route.getRouteOption())
                .providerRank(providerRank)
                .metrics(
                        RouteMetrics.builder()
                                .totalTimeSec(
                                        summary.getTotalTimeSec()
                                )
                                .totalWalkTimeSec(
                                        summary.getTotalTimeSec()
                                )
                                .totalWalkDistanceM(
                                        summary.getTotalDistanceM()
                                )
                                .transferCount(0)
                                .build()
                )
                .walkSegments(
                        List.of(
                                RouteWalkSegment.builder()
                                        .walkSegmentId(
                                                route.getRouteId()
                                                        + ":walk:0"
                                        )
                                        .role(
                                                RouteWalkSegment.Role.WALKING_ROUTE
                                        )
                                        .segmentScope(
                                                SegmentScope.EXTERNAL_WALK
                                        )
                                        .distanceM(
                                                summary.getTotalDistanceM()
                                        )
                                        .durationSec(
                                                summary.getTotalTimeSec()
                                        )
                                        .geometry(
                                                walkingGeometry(route)
                                        )
                                        .accessibilitySignals(
                                                route.getAccessibilitySignals()
                                        )
                                        .build()
                        )
                )
                .build();
    }

    public List<RouteCandidate> fromTransitRoutes(
            TransitRouteResponse response
    ) {
        if (response == null
                || response.getRoutes() == null
                || response.getRoutes().isEmpty()) {
            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }

        return response.getRoutes()
                .stream()
                .map(this::fromTransitRoute)
                .toList();
    }

    private RouteCandidate fromTransitRoute(
            TransitRouteItemResponse route
    ) {
        if (route == null
                || route.getRouteId() == null
                || route.getSummary() == null) {
            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }

        TransitRouteSummaryResponse summary =
                route.getSummary();

        if (summary.getTotalTimeSec() == null
                || summary.getTotalWalkTimeSec() == null
                || summary.getTotalWalkDistanceM() == null
                || summary.getTransferCount() == null) {
            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }

        return RouteCandidate.builder()
                .routeId(route.getRouteId())
                .routeType(RouteType.TRANSIT)
                .providerRank(route.getProviderRank())
                .metrics(
                        RouteMetrics.builder()
                                .totalTimeSec(
                                        summary.getTotalTimeSec()
                                )
                                .totalWalkTimeSec(
                                        summary.getTotalWalkTimeSec()
                                )
                                .totalWalkDistanceM(
                                        summary.getTotalWalkDistanceM()
                                )
                                .transferCount(
                                        summary.getTransferCount()
                                )
                                .build()
                )
                .walkSegments(
                        transitWalkSegments(route)
                )
                .build();
    }

    private List<RouteWalkSegment> transitWalkSegments(
            TransitRouteItemResponse route
    ) {
        List<TransitLegResponse> legs = route.getLegs();
        if (legs == null || legs.isEmpty()) {
            return List.of();
        }

        int firstTransitIndex = -1;
        int lastTransitIndex = -1;
        for (int index = 0; index < legs.size(); index++) {
            TransitLegResponse leg = legs.get(index);
            if (leg != null && !isWalk(leg.getMode())) {
                if (firstTransitIndex < 0) {
                    firstTransitIndex = index;
                }
                lastTransitIndex = index;
            }
        }

        List<RouteWalkSegment> segments = new ArrayList<>();
        for (int legIndex = 0; legIndex < legs.size(); legIndex++) {
            TransitLegResponse leg = legs.get(legIndex);
            if (leg == null || !isWalk(leg.getMode())) {
                continue;
            }

            RouteWalkSegment.Role role = transitWalkRole(
                    legIndex,
                    firstTransitIndex,
                    lastTransitIndex
            );
            segments.add(
                    RouteWalkSegment.builder()
                            .walkSegmentId(
                                    route.getRouteId()
                                            + ":walk:"
                                            + segments.size()
                            )
                            .role(role)
                            .segmentScope(
                                    role == RouteWalkSegment.Role.TRANSFER_WALK
                                            ? SegmentScope.UNKNOWN
                                            : SegmentScope.EXTERNAL_WALK
                            )
                            .distanceM(leg.getDistanceM())
                            .durationSec(leg.getDurationSec())
                            .geometry(transitGeometry(leg))
                            .build()
            );
        }
        return List.copyOf(segments);
    }

    private RouteWalkSegment.Role transitWalkRole(
            int legIndex,
            int firstTransitIndex,
            int lastTransitIndex
    ) {
        if (firstTransitIndex < 0) {
            return RouteWalkSegment.Role.WALKING_ROUTE;
        }
        if (legIndex < firstTransitIndex) {
            return RouteWalkSegment.Role.ORIGIN_TO_FIRST_STOP;
        }
        if (legIndex > lastTransitIndex) {
            return RouteWalkSegment.Role.LAST_STOP_TO_DESTINATION;
        }
        return RouteWalkSegment.Role.TRANSFER_WALK;
    }

    private RouteWalkSegment.Geometry walkingGeometry(
            WalkingRouteItemResponse route
    ) {
        List<List<Double>> coordinates = walkingCoordinates(
                route.getRoutePoints()
        );
        if (coordinates.size() < 2) {
            coordinates = walkingStepCoordinates(
                    route.getSteps()
            );
        }
        return geometry(coordinates);
    }

    private List<List<Double>> walkingCoordinates(
            List<RoutePointResponse> points
    ) {
        List<List<Double>> coordinates = new ArrayList<>();
        for (RoutePointResponse point : points == null ? List.<RoutePointResponse>of() : points) {
            if (point != null) {
                addCoordinate(
                        coordinates,
                        point.getLongitude(),
                        point.getLatitude()
                );
            }
        }
        return coordinates;
    }

    private List<List<Double>> walkingStepCoordinates(
            List<WalkingStepResponse> steps
    ) {
        List<List<Double>> coordinates = new ArrayList<>();
        for (WalkingStepResponse step : steps == null ? List.<WalkingStepResponse>of() : steps) {
            if (step == null) {
                continue;
            }
            for (RoutePointResponse point : step.getPoints() == null
                    ? List.<RoutePointResponse>of()
                    : step.getPoints()) {
                if (point != null) {
                    addCoordinate(
                            coordinates,
                            point.getLongitude(),
                            point.getLatitude()
                    );
                }
            }
        }
        return coordinates;
    }

    private RouteWalkSegment.Geometry transitGeometry(
            TransitLegResponse leg
    ) {
        List<List<Double>> coordinates = transitCoordinates(
                leg.getRoutePoints()
        );
        if (coordinates.size() < 2) {
            coordinates = transitStepCoordinates(
                    leg.getSteps()
            );
        }
        return geometry(coordinates);
    }

    private List<List<Double>> transitCoordinates(
            List<TransitRoutePointResponse> points
    ) {
        List<List<Double>> coordinates = new ArrayList<>();
        for (TransitRoutePointResponse point : points == null
                ? List.<TransitRoutePointResponse>of()
                : points) {
            if (point != null) {
                addCoordinate(
                        coordinates,
                        point.getLongitude(),
                        point.getLatitude()
                );
            }
        }
        return coordinates;
    }

    private List<List<Double>> transitStepCoordinates(
            List<TransitWalkingStepResponse> steps
    ) {
        List<List<Double>> coordinates = new ArrayList<>();
        for (TransitWalkingStepResponse step : steps == null
                ? List.<TransitWalkingStepResponse>of()
                : steps) {
            if (step == null) {
                continue;
            }
            for (TransitRoutePointResponse point : step.getPoints() == null
                    ? List.<TransitRoutePointResponse>of()
                    : step.getPoints()) {
                if (point != null) {
                    addCoordinate(
                            coordinates,
                            point.getLongitude(),
                            point.getLatitude()
                    );
                }
            }
        }
        return coordinates;
    }

    private RouteWalkSegment.Geometry geometry(
            List<List<Double>> coordinates
    ) {
        if (coordinates.size() < 2) {
            return null;
        }
        return RouteWalkSegment.Geometry.builder()
                .type("LineString")
                .coordinates(List.copyOf(coordinates))
                .build();
    }

    private void addCoordinate(
            List<List<Double>> coordinates,
            Double longitude,
            Double latitude
    ) {
        if (longitude == null
                || latitude == null
                || !Double.isFinite(longitude)
                || !Double.isFinite(latitude)
                || longitude < -180
                || longitude > 180
                || latitude < -90
                || latitude > 90) {
            return;
        }

        List<Double> coordinate = List.of(
                longitude,
                latitude
        );
        if (coordinates.isEmpty()
                || !coordinates.get(coordinates.size() - 1)
                .equals(coordinate)) {
            coordinates.add(coordinate);
        }
    }

    private boolean isWalk(String mode) {
        return mode != null
                && "WALK".equalsIgnoreCase(mode.trim());
    }
}
