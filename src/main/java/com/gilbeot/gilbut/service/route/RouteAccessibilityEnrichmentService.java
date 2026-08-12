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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class RouteAccessibilityEnrichmentService {

    private static final String COORD_TYPE =
            "WGS84GEO";

    private static final String SEARCH_OPTION =
            "0";

    private final TmapWalkingRouteClient
            tmapWalkingRouteClient;

    private final WalkingAccessibilitySignalExtractor
            walkingAccessibilitySignalExtractor;

    private final Executor
            routeAccessibilityExecutor;

    public RouteAccessibilityEnrichmentService(
            TmapWalkingRouteClient tmapWalkingRouteClient,
            WalkingAccessibilitySignalExtractor
                    walkingAccessibilitySignalExtractor,
            @Qualifier("routeAccessibilityExecutor")
            Executor routeAccessibilityExecutor
    ) {
        this.tmapWalkingRouteClient =
                tmapWalkingRouteClient;
        this.walkingAccessibilitySignalExtractor =
                walkingAccessibilitySignalExtractor;
        this.routeAccessibilityExecutor =
                routeAccessibilityExecutor;
    }

    public RouteCandidateResult enrich(
            RouteCandidateResult candidateResult
    ) {
        if (candidateResult == null) {
            return null;
        }

        List<RouteCandidate> candidates =
                candidateResult.getCandidates();

        if (candidates == null
                || candidates.isEmpty()) {
            return candidateResult;
        }

        Map<
                SegmentCoordinates,
                CompletableFuture<RouteAccessibilitySignals>
                > signalFutures =
                createSignalFutures(candidates);

        List<RouteCandidate> enrichedCandidates =
                candidates.stream()
                        .map(
                                candidate ->
                                        enrichCandidate(
                                                candidate,
                                                signalFutures
                                        )
                        )
                        .toList();

        return RouteCandidateResult.builder()
                .requestId(
                        candidateResult.getRequestId()
                )
                .candidates(enrichedCandidates)
                .walkingRoute(
                        candidateResult.getWalkingRoute()
                )
                .transitRoutes(
                        candidateResult.getTransitRoutes()
                )
                .build();
    }

    private Map<
            SegmentCoordinates,
            CompletableFuture<RouteAccessibilitySignals>
            > createSignalFutures(
            List<RouteCandidate> candidates
    ) {
        Map<
                SegmentCoordinates,
                CompletableFuture<RouteAccessibilitySignals>
                > futures =
                new LinkedHashMap<>();

        for (RouteCandidate candidate : candidates) {
            if (!isTransitCandidate(candidate)) {
                continue;
            }

            List<RouteWalkSegment> segments =
                    candidate.getWalkSegments();

            if (segments == null
                    || segments.isEmpty()) {
                continue;
            }

            for (RouteWalkSegment segment : segments) {
                if (!shouldEnrich(segment)) {
                    continue;
                }

                SegmentCoordinates coordinates =
                        extractCoordinates(segment);

                if (coordinates == null) {
                    continue;
                }

                futures.computeIfAbsent(
                        coordinates,
                        key ->
                                CompletableFuture.supplyAsync(
                                        () ->
                                                lookupAccessibilitySignals(
                                                        key
                                                ),
                                        routeAccessibilityExecutor
                                )
                );
            }
        }

        return futures;
    }

    private RouteCandidate enrichCandidate(
            RouteCandidate candidate,
            Map<
                    SegmentCoordinates,
                    CompletableFuture<RouteAccessibilitySignals>
                    > signalFutures
    ) {
        if (!isTransitCandidate(candidate)) {
            return candidate;
        }

        List<RouteWalkSegment> segments =
                candidate.getWalkSegments();

        if (segments == null
                || segments.isEmpty()) {
            return candidate;
        }

        List<RouteWalkSegment> enrichedSegments =
                segments.stream()
                        .map(
                                segment ->
                                        enrichSegment(
                                                segment,
                                                signalFutures
                                        )
                        )
                        .toList();

        return candidate.toBuilder()
                .walkSegments(enrichedSegments)
                .build();
    }

    private RouteWalkSegment enrichSegment(
            RouteWalkSegment segment,
            Map<
                    SegmentCoordinates,
                    CompletableFuture<RouteAccessibilitySignals>
                    > signalFutures
    ) {
        if (!shouldEnrich(segment)) {
            return segment;
        }

        SegmentCoordinates coordinates =
                extractCoordinates(segment);

        if (coordinates == null) {
            return segment;
        }

        CompletableFuture<RouteAccessibilitySignals>
                signalFuture =
                signalFutures.get(coordinates);

        if (signalFuture == null) {
            return segment;
        }

        RouteAccessibilitySignals signals =
                signalFuture.join();

        return segment.toBuilder()
                .accessibilitySignals(signals)
                .build();
    }

    private RouteAccessibilitySignals
    lookupAccessibilitySignals(
            SegmentCoordinates coordinates
    ) {
        try {
            TmapWalkingRouteResponse response =
                    tmapWalkingRouteClient.search(
                            toWalkingRouteRequest(
                                    coordinates
                            )
                    );

            return walkingAccessibilitySignalExtractor
                    .extract(response);

        } catch (CustomException e) {
            if (e.getErrorCode()
                    == ErrorCode.ROUTE_SEARCH_FAILED) {

                return RouteAccessibilitySignals
                        .unknown();
            }

            throw e;
        }
    }

    private TmapWalkingRouteRequest
    toWalkingRouteRequest(
            SegmentCoordinates coordinates
    ) {
        return TmapWalkingRouteRequest.builder()
                .startX(
                        coordinates.startLongitude()
                )
                .startY(
                        coordinates.startLatitude()
                )
                .endX(
                        coordinates.endLongitude()
                )
                .endY(
                        coordinates.endLatitude()
                )
                .reqCoordType(COORD_TYPE)
                .resCoordType(COORD_TYPE)
                .startName(
                        "도보 구간 출발지"
                )
                .endName(
                        "도보 구간 도착지"
                )
                .searchOption(SEARCH_OPTION)
                .build();
    }

    private boolean isTransitCandidate(
            RouteCandidate candidate
    ) {
        return candidate != null
                && candidate.getRouteType()
                == RouteType.TRANSIT;
    }

    private boolean shouldEnrich(
            RouteWalkSegment segment
    ) {
        return segment != null
                && segment.getSegmentScope()
                == SegmentScope.EXTERNAL_WALK
                && segment.getAccessibilitySignals()
                == null;
    }

    private SegmentCoordinates extractCoordinates(
            RouteWalkSegment segment
    ) {
        RouteWalkSegment.Geometry geometry =
                segment.getGeometry();

        if (geometry == null
                || geometry.getCoordinates()
                == null
                || geometry.getCoordinates().size()
                < 2) {
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

        if (!isValidCoordinate(start)
                || !isValidCoordinate(end)) {
            return null;
        }

        return new SegmentCoordinates(
                start.get(0),
                start.get(1),
                end.get(0),
                end.get(1)
        );
    }

    private boolean isValidCoordinate(
            List<Double> coordinate
    ) {
        if (coordinate == null
                || coordinate.size() < 2) {
            return false;
        }

        Double longitude =
                coordinate.get(0);

        Double latitude =
                coordinate.get(1);

        if (longitude == null
                || latitude == null) {
            return false;
        }

        if (!Double.isFinite(longitude)
                || !Double.isFinite(latitude)) {
            return false;
        }

        return longitude >= -180.0
                && longitude <= 180.0
                && latitude >= -90.0
                && latitude <= 90.0;
    }

    private record SegmentCoordinates(
            Double startLongitude,
            Double startLatitude,
            Double endLongitude,
            Double endLatitude
    ) {
    }
}