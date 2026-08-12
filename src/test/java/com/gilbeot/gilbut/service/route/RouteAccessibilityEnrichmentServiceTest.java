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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteAccessibilityEnrichmentServiceTest {

    @Mock
    private TmapWalkingRouteClient
            tmapWalkingRouteClient;

    @Mock
    private WalkingAccessibilitySignalExtractor
            walkingAccessibilitySignalExtractor;

    private ExecutorService executorService;

    private RouteAccessibilityEnrichmentService
            routeAccessibilityEnrichmentService;

    @BeforeEach
    void setUp() {
        executorService =
                Executors.newFixedThreadPool(3);

        routeAccessibilityEnrichmentService =
                new RouteAccessibilityEnrichmentService(
                        tmapWalkingRouteClient,
                        walkingAccessibilitySignalExtractor,
                        executorService
                );
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    @DisplayName(
            "대중교통 외부 도보 구간의 접근성 정보를 조회한다"
    )
    void enrichesExternalWalkSegments() {
        TmapWalkingRouteResponse response =
                new TmapWalkingRouteResponse();

        RouteAccessibilitySignals signals =
                RouteAccessibilitySignals.known(
                        1,
                        0,
                        0
                );

        when(
                tmapWalkingRouteClient.search(
                        any(TmapWalkingRouteRequest.class)
                )
        ).thenReturn(response);

        when(
                walkingAccessibilitySignalExtractor
                        .extract(response)
        ).thenReturn(signals);

        RouteWalkSegment firstSegment =
                externalWalkSegment(
                        "transit-1:walk:0",
                        127.0286,
                        37.2636,
                        127.0290,
                        37.2640
                );

        RouteWalkSegment secondSegment =
                externalWalkSegment(
                        "transit-1:walk:1",
                        127.0400,
                        37.2700,
                        127.0410,
                        37.2710
                );

        RouteCandidateResult result =
                routeAccessibilityEnrichmentService
                        .enrich(
                                candidateResult(
                                        List.of(
                                                firstSegment,
                                                secondSegment
                                        )
                                )
                        );

        List<RouteWalkSegment> enrichedSegments =
                result.getCandidates()
                        .get(0)
                        .getWalkSegments();

        assertThat(
                enrichedSegments.get(0)
                        .getAccessibilitySignals()
        ).isSameAs(signals);

        assertThat(
                enrichedSegments.get(1)
                        .getAccessibilitySignals()
        ).isSameAs(signals);

        verify(
                tmapWalkingRouteClient,
                times(2)
        ).search(
                any(TmapWalkingRouteRequest.class)
        );
    }

    @Test
    @DisplayName(
            "동일한 좌표의 외부 도보 구간은 한 번만 조회한다"
    )
    void reusesDuplicateCoordinateLookup() {
        TmapWalkingRouteResponse response =
                new TmapWalkingRouteResponse();

        RouteAccessibilitySignals signals =
                RouteAccessibilitySignals.known(
                        0,
                        0,
                        0
                );

        when(
                tmapWalkingRouteClient.search(
                        any(TmapWalkingRouteRequest.class)
                )
        ).thenReturn(response);

        when(
                walkingAccessibilitySignalExtractor
                        .extract(response)
        ).thenReturn(signals);

        RouteWalkSegment firstSegment =
                externalWalkSegment(
                        "transit-1:walk:0",
                        127.0286,
                        37.2636,
                        127.0290,
                        37.2640
                );

        RouteWalkSegment duplicateSegment =
                externalWalkSegment(
                        "transit-2:walk:0",
                        127.0286,
                        37.2636,
                        127.0290,
                        37.2640
                );

        RouteCandidateResult result =
                routeAccessibilityEnrichmentService
                        .enrich(
                                candidateResult(
                                        List.of(
                                                firstSegment,
                                                duplicateSegment
                                        )
                                )
                        );

        List<RouteWalkSegment> enrichedSegments =
                result.getCandidates()
                        .get(0)
                        .getWalkSegments();

        assertThat(
                enrichedSegments.get(0)
                        .getAccessibilitySignals()
        ).isSameAs(signals);

        assertThat(
                enrichedSegments.get(1)
                        .getAccessibilitySignals()
        ).isSameAs(signals);

        verify(
                tmapWalkingRouteClient,
                times(1)
        ).search(
                any(TmapWalkingRouteRequest.class)
        );
    }

    @Test
    @DisplayName(
            "TMAP 도보 조회 실패 시 접근성 정보를 UNKNOWN으로 처리한다"
    )
    void fallsBackToUnknownWhenLookupFails() {
        when(
                tmapWalkingRouteClient.search(
                        any(TmapWalkingRouteRequest.class)
                )
        ).thenThrow(
                new CustomException(
                        ErrorCode.ROUTE_SEARCH_FAILED
                )
        );

        RouteWalkSegment segment =
                externalWalkSegment(
                        "transit-1:walk:0",
                        127.0286,
                        37.2636,
                        127.0290,
                        37.2640
                );

        RouteCandidateResult result =
                routeAccessibilityEnrichmentService
                        .enrich(
                                candidateResult(
                                        List.of(segment)
                                )
                        );

        RouteAccessibilitySignals signals =
                result.getCandidates()
                        .get(0)
                        .getWalkSegments()
                        .get(0)
                        .getAccessibilitySignals();

        assertThat(signals)
                .isNotNull();

        assertThat(
                signals.getStair()
                        .getState()
                        .name()
        ).isEqualTo("UNKNOWN");

        assertThat(
                signals.getStair()
                        .getCount()
        ).isNull();

        assertThat(
                signals.getOverpass()
                        .getState()
                        .name()
        ).isEqualTo("UNKNOWN");

        assertThat(
                signals.getUnderpass()
                        .getState()
                        .name()
        ).isEqualTo("UNKNOWN");
    }

    @Test
    @DisplayName(
            "대중교통 환승 도보 구간은 보행자 API로 재조회하지 않는다"
    )
    void skipsTransferWalkSegment() {
        RouteWalkSegment transferSegment =
                RouteWalkSegment.builder()
                        .walkSegmentId(
                                "transit-1:walk:1"
                        )
                        .role(
                                RouteWalkSegment.Role
                                        .TRANSFER_WALK
                        )
                        .segmentScope(
                                SegmentScope.UNKNOWN
                        )
                        .distanceM(100)
                        .durationSec(90)
                        .geometry(
                                geometry(
                                        127.0286,
                                        37.2636,
                                        127.0290,
                                        37.2640
                                )
                        )
                        .build();

        RouteCandidateResult result =
                routeAccessibilityEnrichmentService
                        .enrich(
                                candidateResult(
                                        List.of(
                                                transferSegment
                                        )
                                )
                        );

        RouteWalkSegment resultSegment =
                result.getCandidates()
                        .get(0)
                        .getWalkSegments()
                        .get(0);

        assertThat(
                resultSegment
                        .getAccessibilitySignals()
        ).isNull();

        verifyNoInteractions(
                tmapWalkingRouteClient,
                walkingAccessibilitySignalExtractor
        );
    }

    private RouteCandidateResult candidateResult(
            List<RouteWalkSegment> segments
    ) {
        RouteCandidate candidate =
                RouteCandidate.builder()
                        .routeId("transit-1")
                        .routeType(
                                RouteType.TRANSIT
                        )
                        .providerRank(1)
                        .walkSegments(segments)
                        .build();

        return RouteCandidateResult.builder()
                .requestId("request-1")
                .candidates(
                        List.of(candidate)
                )
                .build();
    }

    private RouteWalkSegment externalWalkSegment(
            String walkSegmentId,
            double startLongitude,
            double startLatitude,
            double endLongitude,
            double endLatitude
    ) {
        return RouteWalkSegment.builder()
                .walkSegmentId(walkSegmentId)
                .role(
                        RouteWalkSegment.Role
                                .ORIGIN_TO_FIRST_STOP
                )
                .segmentScope(
                        SegmentScope.EXTERNAL_WALK
                )
                .distanceM(100)
                .durationSec(90)
                .geometry(
                        geometry(
                                startLongitude,
                                startLatitude,
                                endLongitude,
                                endLatitude
                        )
                )
                .build();
    }

    private RouteWalkSegment.Geometry geometry(
            double startLongitude,
            double startLatitude,
            double endLongitude,
            double endLatitude
    ) {
        return RouteWalkSegment.Geometry.builder()
                .type("LineString")
                .coordinates(
                        List.of(
                                List.of(
                                        startLongitude,
                                        startLatitude
                                ),
                                List.of(
                                        endLongitude,
                                        endLatitude
                                )
                        )
                )
                .build();
    }
}