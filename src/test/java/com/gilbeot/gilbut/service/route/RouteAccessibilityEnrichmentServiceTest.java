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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteAccessibilityEnrichmentServiceTest {

    @Mock
    private TmapWalkingRouteClient tmapWalkingRouteClient;

    private RouteAccessibilityEnrichmentService service;

    @BeforeEach
    void setUp() {
        service =
                new RouteAccessibilityEnrichmentService(
                        tmapWalkingRouteClient,
                        new WalkingAccessibilitySignalExtractor()
                );
    }

    @Test
    @DisplayName("대중교통 외부 도보 구간을 보행자 API로 재조회해 접근성 신호를 추가한다")
    void enrichExternalWalkSegments() {
        when(
                tmapWalkingRouteClient.search(any())
        ).thenReturn(
                stairResponse(),
                stairResponse()
        );

        RouteCandidate candidate =
                transitCandidate(
                        List.of(
                                externalSegment(
                                        "walk-1",
                                        RouteWalkSegment.Role.ORIGIN_TO_FIRST_STOP,
                                        127.0000,
                                        37.0000,
                                        127.0010,
                                        37.0010
                                ),
                                transferSegment(),
                                externalSegment(
                                        "walk-3",
                                        RouteWalkSegment.Role.LAST_STOP_TO_DESTINATION,
                                        127.0100,
                                        37.0100,
                                        127.0110,
                                        37.0110
                                )
                        )
                );

        RouteCandidateResult result =
                service.enrich(
                        candidateResult(candidate)
                );

        List<RouteWalkSegment> segments =
                result.getCandidates()
                        .get(0)
                        .getWalkSegments();

        assertThat(
                segments.get(0)
                        .getAccessibilitySignals()
                        .getStair()
                        .getState()
        ).isEqualTo(
                RouteAccessibilitySignals.State.PRESENT
        );

        assertThat(
                segments.get(0)
                        .getAccessibilitySignals()
                        .getStair()
                        .getCount()
        ).isEqualTo(1);

        assertThat(
                segments.get(1)
                        .getAccessibilitySignals()
        ).isNull();

        assertThat(
                segments.get(2)
                        .getAccessibilitySignals()
                        .getStair()
                        .getState()
        ).isEqualTo(
                RouteAccessibilitySignals.State.PRESENT
        );

        verify(
                tmapWalkingRouteClient,
                times(2)
        ).search(any());
    }

    @Test
    @DisplayName("보행자 API 재조회가 실패하면 접근성 신호를 UNKNOWN으로 처리한다")
    void markUnknownWhenPedestrianLookupFails() {
        when(
                tmapWalkingRouteClient.search(any())
        ).thenThrow(
                new CustomException(
                        ErrorCode.ROUTE_SEARCH_FAILED
                )
        );

        RouteCandidate candidate =
                transitCandidate(
                        List.of(
                                externalSegment(
                                        "walk-1",
                                        RouteWalkSegment.Role.ORIGIN_TO_FIRST_STOP,
                                        127.0000,
                                        37.0000,
                                        127.0010,
                                        37.0010
                                )
                        )
                );

        RouteCandidateResult result =
                service.enrich(
                        candidateResult(candidate)
                );

        RouteAccessibilitySignals signals =
                result.getCandidates()
                        .get(0)
                        .getWalkSegments()
                        .get(0)
                        .getAccessibilitySignals();

        assertThat(
                signals.getStair().getState()
        ).isEqualTo(
                RouteAccessibilitySignals.State.UNKNOWN
        );

        assertThat(
                signals.getStair().getCount()
        ).isNull();

        assertThat(
                signals.getOverpass().getState()
        ).isEqualTo(
                RouteAccessibilitySignals.State.UNKNOWN
        );

        assertThat(
                signals.getUnderpass().getState()
        ).isEqualTo(
                RouteAccessibilitySignals.State.UNKNOWN
        );
    }

    @Test
    @DisplayName("동일한 시작 종료 좌표의 도보 구간은 보행자 API를 한 번만 조회한다")
    void reuseLookupResultForSameCoordinates() {
        when(
                tmapWalkingRouteClient.search(any())
        ).thenReturn(
                stairResponse()
        );

        RouteWalkSegment first =
                externalSegment(
                        "walk-1",
                        RouteWalkSegment.Role.ORIGIN_TO_FIRST_STOP,
                        127.0000,
                        37.0000,
                        127.0010,
                        37.0010
                );

        RouteWalkSegment second =
                externalSegment(
                        "walk-2",
                        RouteWalkSegment.Role.LAST_STOP_TO_DESTINATION,
                        127.0000,
                        37.0000,
                        127.0010,
                        37.0010
                );

        RouteCandidate firstCandidate =
                transitCandidate(
                        List.of(first)
                );

        RouteCandidate secondCandidate =
                RouteCandidate.builder()
                        .routeId("transit-2")
                        .routeType(RouteType.TRANSIT)
                        .walkSegments(
                                List.of(second)
                        )
                        .build();

        RouteCandidateResult candidateResult =
                RouteCandidateResult.builder()
                        .requestId("request-1")
                        .candidates(
                                List.of(
                                        firstCandidate,
                                        secondCandidate
                                )
                        )
                        .build();

        RouteCandidateResult result =
                service.enrich(
                        candidateResult
                );

        assertThat(
                result.getCandidates()
                        .get(0)
                        .getWalkSegments()
                        .get(0)
                        .getAccessibilitySignals()
                        .getStair()
                        .getState()
        ).isEqualTo(
                RouteAccessibilitySignals.State.PRESENT
        );

        assertThat(
                result.getCandidates()
                        .get(1)
                        .getWalkSegments()
                        .get(0)
                        .getAccessibilitySignals()
                        .getStair()
                        .getState()
        ).isEqualTo(
                RouteAccessibilitySignals.State.PRESENT
        );

        verify(
                tmapWalkingRouteClient,
                times(1)
        ).search(any());
    }

    @Test
    @DisplayName("보행자 API 재조회에는 도보 구간 geometry의 시작 종료 좌표를 사용한다")
    void useSegmentGeometryCoordinates() {
        when(
                tmapWalkingRouteClient.search(any())
        ).thenReturn(
                stairResponse()
        );

        RouteCandidate candidate =
                transitCandidate(
                        List.of(
                                externalSegment(
                                        "walk-1",
                                        RouteWalkSegment.Role.ORIGIN_TO_FIRST_STOP,
                                        127.1234,
                                        37.1234,
                                        127.5678,
                                        37.5678
                                )
                        )
                );

        service.enrich(
                candidateResult(candidate)
        );

        ArgumentCaptor<TmapWalkingRouteRequest> captor =
                ArgumentCaptor.forClass(
                        TmapWalkingRouteRequest.class
                );

        verify(
                tmapWalkingRouteClient
        ).search(
                captor.capture()
        );

        TmapWalkingRouteRequest request =
                captor.getValue();

        assertThat(request.getStartX())
                .isEqualTo(127.1234);

        assertThat(request.getStartY())
                .isEqualTo(37.1234);

        assertThat(request.getEndX())
                .isEqualTo(127.5678);

        assertThat(request.getEndY())
                .isEqualTo(37.5678);

        assertThat(request.getSearchOption())
                .isEqualTo("0");
    }

    private RouteCandidateResult candidateResult(
            RouteCandidate candidate
    ) {
        return RouteCandidateResult.builder()
                .requestId("request-1")
                .candidates(
                        List.of(candidate)
                )
                .build();
    }

    private RouteCandidate transitCandidate(
            List<RouteWalkSegment> walkSegments
    ) {
        return RouteCandidate.builder()
                .routeId("transit-1")
                .routeType(RouteType.TRANSIT)
                .providerRank(1)
                .walkSegments(walkSegments)
                .build();
    }

    private RouteWalkSegment externalSegment(
            String walkSegmentId,
            RouteWalkSegment.Role role,
            double startLongitude,
            double startLatitude,
            double endLongitude,
            double endLatitude
    ) {
        return RouteWalkSegment.builder()
                .walkSegmentId(walkSegmentId)
                .role(role)
                .segmentScope(
                        SegmentScope.EXTERNAL_WALK
                )
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

    private RouteWalkSegment transferSegment() {
        return RouteWalkSegment.builder()
                .walkSegmentId("transfer-walk")
                .role(
                        RouteWalkSegment.Role.TRANSFER_WALK
                )
                .segmentScope(
                        SegmentScope.UNKNOWN
                )
                .geometry(
                        geometry(
                                127.0200,
                                37.0200,
                                127.0210,
                                37.0210
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

    private TmapWalkingRouteResponse stairResponse() {
        TmapWalkingRouteResponse.Properties properties =
                new TmapWalkingRouteResponse.Properties();

        properties.setFacilityType(17);

        TmapWalkingRouteResponse.Geometry geometry =
                new TmapWalkingRouteResponse.Geometry();

        geometry.setType("LineString");

        TmapWalkingRouteResponse.Feature feature =
                new TmapWalkingRouteResponse.Feature();

        feature.setGeometry(geometry);
        feature.setProperties(properties);

        TmapWalkingRouteResponse response =
                new TmapWalkingRouteResponse();

        response.setFeatures(
                List.of(feature)
        );

        return response;
    }
}