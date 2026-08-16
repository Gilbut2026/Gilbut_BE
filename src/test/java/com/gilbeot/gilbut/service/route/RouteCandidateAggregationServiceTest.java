package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
import com.gilbeot.gilbut.dto.route.RouteCandidateResult;
import com.gilbeot.gilbut.dto.route.TransitRouteFailureCode;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteItemResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteSummaryResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteItemResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteSummaryResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.global.exception.TransitRouteSearchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteCandidateAggregationServiceTest {

    @Mock
    private WalkingRouteService walkingRouteService;

    @Mock
    private TransitRouteService transitRouteService;

    private RouteCandidateAggregationService
            routeCandidateAggregationService;

    @BeforeEach
    void setUp() {
        routeCandidateAggregationService =
                new RouteCandidateAggregationService(
                        walkingRouteService,
                        transitRouteService,
                        new RouteCandidateMapper()
                );
    }

    @Test
    @DisplayName("상세 보행 경로와 대중교통 경로를 동일한 routeId의 추천 후보로 변환한다")
    void createCandidates() {

        WalkingRouteResponse walkingRoute =
                createWalkingRoute();

        TransitRouteResponse transitRoutes =
                createTransitRoutes();

        when(walkingRouteService.search(any()))
                .thenReturn(walkingRoute);

        when(transitRouteService.search(any()))
                .thenReturn(transitRoutes);

        RouteCandidateResult result =
                routeCandidateAggregationService
                        .createCandidates(
                                createRequest()
                        );

        assertThat(result.getRequestId())
                .isNotBlank();

        assertThat(result.getCandidates())
                .hasSize(3);

        assertThat(
                result.getCandidates()
                        .get(0)
                        .getRouteId()
        ).isEqualTo(
                walkingRoute.getRoutes()
                        .get(0)
                        .getRouteId()
        );

        assertThat(
                result.getCandidates()
                        .get(0)
                        .getRouteType()
        ).isEqualTo(
                RouteType.WALKING
        );

        assertThat(
                result.getCandidates()
                        .get(1)
                        .getRouteId()
        ).isEqualTo(
                walkingRoute.getRoutes()
                        .get(1)
                        .getRouteId()
        );

        assertThat(
                result.getCandidates()
                        .get(1)
                        .getRouteType()
        ).isEqualTo(
                RouteType.WALKING
        );

        assertThat(
                result.getCandidates()
                        .get(2)
                        .getRouteId()
        ).isEqualTo(
                transitRoutes.getRoutes()
                        .get(0)
                        .getRouteId()
        );

        assertThat(
                result.getCandidates()
                        .get(2)
                        .getRouteType()
        ).isEqualTo(
                RouteType.TRANSIT
        );

        assertThat(
                result.getWalkingRoute()
        ).isSameAs(
                walkingRoute
        );

        assertThat(
                result.getTransitRoutes()
        ).isSameAs(
                transitRoutes
        );

        verify(walkingRouteService)
                .search(any());

        verify(transitRouteService)
                .search(any());
    }

    @Test
    @DisplayName("보행 경로 조회가 실패해도 대중교통 경로를 반환한다")
    void createCandidatesWhenWalkingFails() {

        when(walkingRouteService.search(any()))
                .thenThrow(
                        new CustomException(
                                ErrorCode.ROUTE_SEARCH_FAILED
                        )
                );

        when(transitRouteService.search(any()))
                .thenReturn(
                        createTransitRoutes()
                );

        RouteCandidateResult result =
                routeCandidateAggregationService
                        .createCandidates(
                                createRequest()
                        );

        assertThat(result.getCandidates())
                .hasSize(1);

        assertThat(
                result.getCandidates()
                        .get(0)
                        .getRouteType()
        ).isEqualTo(
                RouteType.TRANSIT
        );

        assertThat(
                result.getWalkingRoute()
        ).isNull();

        assertThat(
                result.getTransitRoutes()
        ).isNotNull();

        assertThat(
                result.getTransitRouteFailure()
        ).isNull();
    }

    @Test
    @DisplayName("대중교통 경로 조회가 실패해도 보행 경로를 반환한다")
    void createCandidatesWhenTransitFails() {

        when(walkingRouteService.search(any()))
                .thenReturn(
                        createWalkingRoute()
                );

        when(transitRouteService.search(any()))
                .thenThrow(
                        new TransitRouteSearchException(
                                TransitRouteFailureCode.QUOTA_EXCEEDED
                        )
                );

        RouteCandidateResult result =
                routeCandidateAggregationService
                        .createCandidates(
                                createRequest()
                        );

        assertThat(result.getCandidates())
                .hasSize(2);

        assertThat(
                result.getCandidates()
                        .get(0)
                        .getRouteType()
        ).isEqualTo(
                RouteType.WALKING
        );

        assertThat(
                result.getCandidates()
                        .get(1)
                        .getRouteType()
        ).isEqualTo(
                RouteType.WALKING
        );

        assertThat(
                result.getWalkingRoute()
        ).isNotNull();

        assertThat(
                result.getTransitRoutes()
        ).isNull();

        assertThat(
                result.getTransitRouteFailure()
                        .getCode()
        ).isEqualTo(
                TransitRouteFailureCode.QUOTA_EXCEEDED
        );

        assertThat(
                result.getTransitRouteFailure()
                        .getMessage()
        ).isEqualTo(
                TransitRouteFailureCode.QUOTA_EXCEEDED
                        .getMessage()
        );
    }

    @Test
    @DisplayName("보행과 대중교통 경로 조회가 모두 실패하면 예외가 발생한다")
    void createCandidatesWhenAllRoutesFail() {

        when(walkingRouteService.search(any()))
                .thenThrow(
                        new CustomException(
                                ErrorCode.ROUTE_SEARCH_FAILED
                        )
                );

        when(transitRouteService.search(any()))
                .thenThrow(
                        new CustomException(
                                ErrorCode.ROUTE_SEARCH_FAILED
                        )
                );

        assertThatThrownBy(() ->
                routeCandidateAggregationService
                        .createCandidates(
                                createRequest()
                        )
        )
                .isInstanceOf(
                        CustomException.class
                )
                .extracting("errorCode")
                .isEqualTo(
                        ErrorCode.ROUTE_SEARCH_FAILED
                );
    }

    private RouteCandidateRequest createRequest() {

        return RouteCandidateRequest.builder()
                .originLatitude(37.2636)
                .originLongitude(127.0286)
                .destinationLatitude(37.279)
                .destinationLongitude(127.047)
                .departureDateTime(
                        LocalDateTime.of(
                                2026,
                                8,
                                8,
                                10,
                                0
                        )
                )
                .build();
    }

    private WalkingRouteResponse createWalkingRoute() {

        WalkingRouteItemResponse defaultRoute =
                WalkingRouteItemResponse.builder()
                        .routeId(
                                "walking-uuid-1"
                        )
                        .routeOption(
                                WalkingRouteOption.DEFAULT
                        )
                        .summary(
                                WalkingRouteSummaryResponse.builder()
                                        .totalDistanceM(
                                                1500
                                        )
                                        .totalTimeSec(
                                                1800
                                        )
                                        .build()
                        )
                        .routePoints(
                                List.of()
                        )
                        .steps(
                                List.of()
                        )
                        .build();

        WalkingRouteItemResponse avoidStairsRoute =
                WalkingRouteItemResponse.builder()
                        .routeId(
                                "walking-avoid-stairs-uuid-1"
                        )
                        .routeOption(
                                WalkingRouteOption.AVOID_STAIRS
                        )
                        .summary(
                                WalkingRouteSummaryResponse.builder()
                                        .totalDistanceM(
                                                1700
                                        )
                                        .totalTimeSec(
                                                2100
                                        )
                                        .build()
                        )
                        .routePoints(
                                List.of()
                        )
                        .steps(
                                List.of()
                        )
                        .build();

        return WalkingRouteResponse.builder()
                .routes(
                        List.of(
                                defaultRoute,
                                avoidStairsRoute
                        )
                )
                .build();
    }

    private TransitRouteResponse createTransitRoutes() {

        TransitRouteItemResponse route =
                TransitRouteItemResponse.builder()
                        .routeId(
                                "transit-uuid-1"
                        )
                        .providerRank(1)
                        .summary(
                                TransitRouteSummaryResponse.builder()
                                        .totalTimeSec(
                                                1500
                                        )
                                        .totalWalkTimeSec(
                                                420
                                        )
                                        .totalWalkDistanceM(
                                                350
                                        )
                                        .transferCount(
                                                1
                                        )
                                        .build()
                        )
                        .routePoints(
                                List.of()
                        )
                        .legs(
                                List.of()
                        )
                        .build();

        return TransitRouteResponse.builder()
                .routes(
                        List.of(route)
                )
                .build();
    }
}
