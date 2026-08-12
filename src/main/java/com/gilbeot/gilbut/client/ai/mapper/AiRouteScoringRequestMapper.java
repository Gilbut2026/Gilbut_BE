package com.gilbeot.gilbut.client.ai.mapper;

import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringRequest;
import com.gilbeot.gilbut.client.ai.dto.scoring.type.AccessibilitySignalState;
import com.gilbeot.gilbut.dto.route.RouteAccessibilitySignals;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteCandidateResult;
import com.gilbeot.gilbut.dto.route.RouteMetrics;
import com.gilbeot.gilbut.dto.route.RouteWalkSegment;
import com.gilbeot.gilbut.dto.user.response.MobilityProfileResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiRouteScoringRequestMapper {

    public AiRouteScoringRequest toRequest(
            MobilityProfileResponse profile,
            RouteCandidateResult routeCandidateResult
    ) {
        return AiRouteScoringRequest.builder()
                .requestId(routeCandidateResult.getRequestId())
                .userContext(toUserContext(profile))
                .candidates(
                        routeCandidateResult.getCandidates()
                                .stream()
                                .map(this::toCandidate)
                                .toList()
                )
                .build();
    }

    private AiRouteScoringRequest.UserContext toUserContext(
            MobilityProfileResponse profile
    ) {
        return AiRouteScoringRequest.UserContext.builder()
                .walkingDuration(profile.getWalkingDuration())
                .stairLevel(profile.getStairLevel())
                .slopeLevel(profile.getSlopeLevel())
                .restStopPreference(profile.getRestStopPreference())
                .transferLevel(profile.getTransferLevel())
                .mobilityAid(profile.getMobilityAid())
                .build();
    }

    private AiRouteScoringRequest.Candidate toCandidate(
            RouteCandidate candidate
    ) {
        return AiRouteScoringRequest.Candidate.builder()
                .routeId(candidate.getRouteId())
                .routeType(candidate.getRouteType())
                .routeOption(candidate.getRouteOption())
                .providerRank(candidate.getProviderRank())
                .metrics(toMetrics(candidate.getMetrics()))
                .walkSegments(
                        candidate.getWalkSegments() == null
                                ? List.of()
                                : candidate.getWalkSegments()
                                .stream()
                                .map(this::toWalkSegment)
                                .toList()
                )
                .build();
    }

    private AiRouteScoringRequest.WalkSegment toWalkSegment(
            RouteWalkSegment segment
    ) {
        return AiRouteScoringRequest.WalkSegment.builder()
                .walkSegmentId(segment.getWalkSegmentId())
                .role(
                        segment.getRole() == null
                                ? null
                                : segment.getRole().name()
                )
                .segmentScope(segment.getSegmentScope())
                .distanceM(segment.getDistanceM())
                .durationSec(segment.getDurationSec())
                .geometry(toGeometry(segment.getGeometry()))
                .accessibilitySignals(
                        toAccessibilitySignals(
                                segment.getAccessibilitySignals()
                        )
                )
                .build();
    }

    private AiRouteScoringRequest.AccessibilitySignals toAccessibilitySignals(
            RouteAccessibilitySignals signals
    ) {
        if (signals == null) {
            return null;
        }

        return AiRouteScoringRequest.AccessibilitySignals.builder()
                .stair(
                        toSignal(
                                signals.getStair()
                        )
                )
                .overpass(
                        toSignal(
                                signals.getOverpass()
                        )
                )
                .underpass(
                        toSignal(
                                signals.getUnderpass()
                        )
                )
                .build();
    }

    private AiRouteScoringRequest.Signal toSignal(
            RouteAccessibilitySignals.Signal signal
    ) {
        if (signal == null) {
            return null;
        }

        return AiRouteScoringRequest.Signal.builder()
                .state(
                        signal.getState() == null
                                ? null
                                : AccessibilitySignalState.valueOf(
                                signal.getState().name()
                        )
                )
                .count(signal.getCount())
                .build();
    }

    private AiRouteScoringRequest.Geometry toGeometry(
            RouteWalkSegment.Geometry geometry
    ) {
        if (geometry == null) {
            return null;
        }

        return AiRouteScoringRequest.Geometry.builder()
                .type(geometry.getType())
                .coordinates(geometry.getCoordinates())
                .build();
    }

    private AiRouteScoringRequest.Metrics toMetrics(
            RouteMetrics metrics
    ) {
        return AiRouteScoringRequest.Metrics.builder()
                .totalTimeSec(metrics.getTotalTimeSec())
                .totalWalkTimeSec(metrics.getTotalWalkTimeSec())
                .totalWalkDistanceM(metrics.getTotalWalkDistanceM())
                .transferCount(metrics.getTransferCount())
                .build();
    }
}