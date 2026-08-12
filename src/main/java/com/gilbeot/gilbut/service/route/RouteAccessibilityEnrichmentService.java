package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.client.ai.dto.scoring.type.SegmentScope;
import com.gilbeot.gilbut.client.tmap.TmapWalkingRouteClient;
import com.gilbeot.gilbut.client.tmap.dto.walking.TmapWalkingRouteRequest;
import com.gilbeot.gilbut.client.tmap.dto.walking.TmapWalkingRouteResponse;
import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.dto.route.RouteAccessibilitySignals;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteCandidateResult;
import com.gilbeot.gilbut.dto.route.RouteWalkSegment;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RouteAccessibilityEnrichmentService {

    private static final String COORD_TYPE = "WGS84GEO";
    private static final String DEFAULT_SEARCH_OPTION = "0";
    private static final String START_NAME = "도보 구간 출발지";
    private static final String END_NAME = "도보 구간 도착지";

    private final TmapWalkingRouteClient tmapWalkingRouteClient;
    private final WalkingAccessibilitySignalExtractor walkingAccessibilitySignalExtractor;

    public RouteCandidateResult enrich(
            RouteCandidateResult candidateResult
    ) {
        Map<SegmentCoordinates, RouteAccessibilitySignals> cache =
                new HashMap<>();

        List<RouteCandidate> enrichedCandidates =
                candidateResult.getCandidates()
                        .stream()
                        .map(candidate ->
                                enrichCandidate(
                                        candidate,
                                        cache
                                )
                        )
                        .toList();

        return RouteCandidateResult.builder()
                .requestId(candidateResult.getRequestId())
                .candidates(enrichedCandidates)
                .walkingRoute(candidateResult.getWalkingRoute())
                .transitRoutes(candidateResult.getTransitRoutes())
                .build();
    }

    private RouteCandidate enrichCandidate(
            RouteCandidate candidate,
            Map<SegmentCoordinates, RouteAccessibilitySignals> cache
    ) {
        if (candidate == null
                || candidate.getRouteType() != RouteType.TRANSIT
                || candidate.getWalkSegments() == null
                || candidate.getWalkSegments().isEmpty()) {
            return candidate;
        }

        List<RouteWalkSegment> enrichedSegments =
                candidate.getWalkSegments()
                        .stream()
                        .map(segment ->
                                enrichSegment(
                                        segment,
                                        cache
                                )
                        )
                        .toList();

        return candidate.toBuilder()
                .walkSegments(enrichedSegments)
                .build();
    }

    private RouteWalkSegment enrichSegment(
            RouteWalkSegment segment,
            Map<SegmentCoordinates, RouteAccessibilitySignals> cache
    ) {
        if (!shouldEnrich(segment)) {
            return segment;
        }

        SegmentCoordinates coordinates =
                extractCoordinates(segment);

        RouteAccessibilitySignals signals;

        if (coordinates == null) {
            signals =
                    RouteAccessibilitySignals.unknown();
        } else {
            signals =
                    cache.computeIfAbsent(
                            coordinates,
                            this::lookupAccessibilitySignals
                    );
        }

        return segment.toBuilder()
                .accessibilitySignals(signals)
                .build();
    }

    private boolean shouldEnrich(
            RouteWalkSegment segment
    ) {
        return segment != null
                && segment.getSegmentScope()
                == SegmentScope.EXTERNAL_WALK
                && segment.getAccessibilitySignals() == null;
    }

    private SegmentCoordinates extractCoordinates(
            RouteWalkSegment segment
    ) {
        RouteWalkSegment.Geometry geometry =
                segment.getGeometry();

        if (geometry == null
                || geometry.getCoordinates() == null
                || geometry.getCoordinates().size() < 2) {
            return null;
        }

        List<List<Double>> coordinates =
                geometry.getCoordinates();

        List<Double> start =
                coordinates.get(0);

        List<Double> end =
                coordinates.get(
                        coordinates.size() - 1
                );

        if (!isValidCoordinatePair(start)
                || !isValidCoordinatePair(end)) {
            return null;
        }

        return new SegmentCoordinates(
                start.get(0),
                start.get(1),
                end.get(0),
                end.get(1)
        );
    }

    private boolean isValidCoordinatePair(
            List<Double> coordinate
    ) {
        if (coordinate == null
                || coordinate.size() < 2
                || coordinate.get(0) == null
                || coordinate.get(1) == null) {
            return false;
        }

        double longitude =
                coordinate.get(0);

        double latitude =
                coordinate.get(1);

        return Double.isFinite(longitude)
                && Double.isFinite(latitude)
                && longitude >= -180
                && longitude <= 180
                && latitude >= -90
                && latitude <= 90
                && !(longitude == 0
                && latitude == 0);
    }

    private RouteAccessibilitySignals lookupAccessibilitySignals(
            SegmentCoordinates coordinates
    ) {
        try {
            TmapWalkingRouteResponse response =
                    tmapWalkingRouteClient.search(
                            toWalkingRouteRequest(
                                    coordinates
                            )
                    );

            return walkingAccessibilitySignalExtractor.extract(
                    response
            );

        } catch (CustomException e) {
            if (e.getErrorCode()
                    == ErrorCode.ROUTE_SEARCH_FAILED) {
                return RouteAccessibilitySignals.unknown();
            }

            throw e;
        }
    }

    private TmapWalkingRouteRequest toWalkingRouteRequest(
            SegmentCoordinates coordinates
    ) {
        return TmapWalkingRouteRequest.builder()
                .startX(coordinates.startLongitude())
                .startY(coordinates.startLatitude())
                .endX(coordinates.endLongitude())
                .endY(coordinates.endLatitude())
                .reqCoordType(COORD_TYPE)
                .resCoordType(COORD_TYPE)
                .startName(START_NAME)
                .endName(END_NAME)
                .searchOption(DEFAULT_SEARCH_OPTION)
                .build();
    }

    private record SegmentCoordinates(
            double startLongitude,
            double startLatitude,
            double endLongitude,
            double endLatitude
    ) {
    }
}