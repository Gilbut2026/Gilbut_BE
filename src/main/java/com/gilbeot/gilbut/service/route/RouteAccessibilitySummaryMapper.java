package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.dto.route.RouteAccessibilitySignals;
import com.gilbeot.gilbut.dto.route.RouteAccessibilitySummary;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteWalkSegment;
import com.gilbeot.gilbut.dto.route.transit.response.TransitLegResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteItemResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitWalkingStepResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteItemResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingStepResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class RouteAccessibilitySummaryMapper {

    public RouteAccessibilitySummary toSummary(
            RouteCandidate candidate,
            WalkingRouteResponse walkingRouteResponse,
            TransitRouteResponse transitRouteResponse
    ) {
        if (hasNoWalking(candidate)) {
            return emptySummary();
        }

        List<RouteWalkSegment> segments =
                relevantSegments(candidate);

        return RouteAccessibilitySummary.builder()
                .stair(
                        aggregateObstacle(
                                segments,
                                RouteAccessibilitySignals::getStair
                        )
                )
                .overpass(
                        aggregateObstacle(
                                segments,
                                RouteAccessibilitySignals::getOverpass
                        )
                )
                .underpass(
                        aggregateObstacle(
                                segments,
                                RouteAccessibilitySignals::getUnderpass
                        )
                )
                .crosswalk(
                        resolveCrosswalk(
                                candidate,
                                walkingRouteResponse,
                                transitRouteResponse
                        )
                )
                .build();
    }

    private List<RouteWalkSegment> relevantSegments(
            RouteCandidate candidate
    ) {
        if (candidate == null
                || candidate.getWalkSegments() == null) {
            return List.of();
        }

        return candidate.getWalkSegments()
                .stream()
                .filter(this::isRelevantSegment)
                .toList();
    }

    private boolean isRelevantSegment(
            RouteWalkSegment segment
    ) {
        if (segment == null) {
            return false;
        }

        Integer distanceM =
                segment.getDistanceM();

        return distanceM == null
                || distanceM > 0;
    }

    private RouteAccessibilitySignals.Signal aggregateObstacle(
            List<RouteWalkSegment> segments,
            Function<
                    RouteAccessibilitySignals,
                    RouteAccessibilitySignals.Signal
                    > selector
    ) {
        if (segments.isEmpty()) {
            return unknownSignal();
        }

        int totalCount = 0;

        for (RouteWalkSegment segment : segments) {
            RouteAccessibilitySignals signals =
                    segment.getAccessibilitySignals();

            if (signals == null) {
                return unknownSignal();
            }

            RouteAccessibilitySignals.Signal signal =
                    selector.apply(signals);

            if (!isKnown(signal)) {
                return unknownSignal();
            }

            totalCount += signal.getCount();
        }

        return knownSignal(totalCount);
    }

    private RouteAccessibilitySignals.Signal resolveCrosswalk(
            RouteCandidate candidate,
            WalkingRouteResponse walkingRouteResponse,
            TransitRouteResponse transitRouteResponse
    ) {
        if (candidate == null
                || candidate.getRouteType() == null) {
            return unknownSignal();
        }

        if (candidate.getRouteType()
                == RouteType.WALKING) {
            return walkingCrosswalkSignal(
                    candidate.getRouteId(),
                    walkingRouteResponse
            );
        }

        if (candidate.getRouteType()
                == RouteType.TRANSIT) {
            return transitCrosswalkSignal(
                    candidate.getRouteId(),
                    transitRouteResponse
            );
        }

        return unknownSignal();
    }

    private RouteAccessibilitySignals.Signal walkingCrosswalkSignal(
            String routeId,
            WalkingRouteResponse response
    ) {
        WalkingRouteItemResponse route =
                findWalkingRoute(
                        routeId,
                        response
                );

        if (route == null
                || route.getSteps() == null
                || route.getSteps().isEmpty()) {
            return unknownSignal();
        }

        int count =
                (int) route.getSteps()
                        .stream()
                        .filter(this::isCrosswalkStep)
                        .count();

        return knownSignal(count);
    }

    private RouteAccessibilitySignals.Signal transitCrosswalkSignal(
            String routeId,
            TransitRouteResponse response
    ) {
        TransitRouteItemResponse route =
                findTransitRoute(
                        routeId,
                        response
                );

        if (route == null
                || route.getLegs() == null) {
            return unknownSignal();
        }

        List<TransitLegResponse> walkLegs =
                route.getLegs()
                        .stream()
                        .filter(this::isRelevantWalkLeg)
                        .toList();

        if (walkLegs.isEmpty()) {
            return unknownSignal();
        }

        int count = 0;

        for (TransitLegResponse leg : walkLegs) {
            List<TransitWalkingStepResponse> steps =
                    leg.getSteps();

            if (steps == null
                    || steps.isEmpty()) {
                return unknownSignal();
            }

            count +=
                    (int) steps.stream()
                            .filter(this::isCrosswalkStep)
                            .count();
        }

        return knownSignal(count);
    }

    private WalkingRouteItemResponse findWalkingRoute(
            String routeId,
            WalkingRouteResponse response
    ) {
        if (routeId == null
                || response == null
                || response.getRoutes() == null) {
            return null;
        }

        return response.getRoutes()
                .stream()
                .filter(route ->
                        route != null
                                && routeId.equals(
                                route.getRouteId()
                        )
                )
                .findFirst()
                .orElse(null);
    }

    private TransitRouteItemResponse findTransitRoute(
            String routeId,
            TransitRouteResponse response
    ) {
        if (routeId == null
                || response == null
                || response.getRoutes() == null) {
            return null;
        }

        return response.getRoutes()
                .stream()
                .filter(route ->
                        route != null
                                && routeId.equals(
                                route.getRouteId()
                        )
                )
                .findFirst()
                .orElse(null);
    }

    private boolean isRelevantWalkLeg(
            TransitLegResponse leg
    ) {
        if (leg == null
                || leg.getMode() == null
                || !"WALK".equalsIgnoreCase(
                leg.getMode().trim()
        )) {
            return false;
        }

        Integer distanceM =
                leg.getDistanceM();

        return distanceM == null
                || distanceM > 0;
    }

    private boolean isCrosswalkStep(
            WalkingStepResponse step
    ) {
        return step != null
                && containsCrosswalk(
                step.getInstruction()
        );
    }

    private boolean isCrosswalkStep(
            TransitWalkingStepResponse step
    ) {
        return step != null
                && containsCrosswalk(
                step.getInstruction()
        );
    }

    private boolean containsCrosswalk(
            String instruction
    ) {
        return instruction != null
                && instruction.contains(
                "횡단보도"
        );
    }

    private boolean hasNoWalking(
            RouteCandidate candidate
    ) {
        return candidate != null
                && candidate.getMetrics() != null
                && Integer.valueOf(0).equals(
                candidate.getMetrics()
                        .getTotalWalkDistanceM()
        );
    }

    private boolean isKnown(
            RouteAccessibilitySignals.Signal signal
    ) {
        return signal != null
                && signal.getState() != null
                && signal.getState()
                != RouteAccessibilitySignals.State.UNKNOWN
                && signal.getCount() != null;
    }

    private RouteAccessibilitySummary emptySummary() {
        RouteAccessibilitySignals.Signal empty =
                knownSignal(0);

        return RouteAccessibilitySummary.builder()
                .stair(empty)
                .overpass(empty)
                .underpass(empty)
                .crosswalk(empty)
                .build();
    }

    private RouteAccessibilitySignals.Signal knownSignal(
            int count
    ) {
        int normalizedCount =
                Math.max(count, 0);

        return RouteAccessibilitySignals.Signal.builder()
                .state(
                        normalizedCount > 0
                                ? RouteAccessibilitySignals.State.PRESENT
                                : RouteAccessibilitySignals.State.ABSENT
                )
                .count(normalizedCount)
                .build();
    }

    private RouteAccessibilitySignals.Signal unknownSignal() {
        return RouteAccessibilitySignals.Signal.builder()
                .state(
                        RouteAccessibilitySignals.State.UNKNOWN
                )
                .count(null)
                .build();
    }
}