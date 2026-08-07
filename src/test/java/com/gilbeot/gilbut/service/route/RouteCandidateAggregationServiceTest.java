package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
import com.gilbeot.gilbut.dto.route.RouteCandidateResult;
import com.gilbeot.gilbut.dto.route.RouteMetrics;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteCandidateAggregationServiceTest {

    @Mock
    private RouteCandidateService routeCandidateService;

    @Mock
    private WalkingRouteCandidateService walkingRouteCandidateService;

    private RouteCandidateAggregationService routeCandidateAggregationService;

    @BeforeEach
    void setUp() {
        routeCandidateAggregationService =
                new RouteCandidateAggregationService(
                        routeCandidateService,
                        walkingRouteCandidateService
                );
    }

    @Test
    @DisplayName("보행 경로와 대중교통 경로 후보를 하나의 결과로 통합한다")
    void createCandidates() {
        RouteCandidate walkingCandidate = createWalkingCandidate();
        RouteCandidate transitCandidate = createTransitCandidate();

        when(walkingRouteCandidateService.createCandidate(any()))
                .thenReturn(walkingCandidate);

        when(routeCandidateService.createCandidates(any()))
                .thenReturn(
                        RouteCandidateResult.builder()
                                .requestId("transit-request")
                                .candidates(List.of(transitCandidate))
                                .build()
                );

        RouteCandidateResult result =
                routeCandidateAggregationService.createCandidates(
                        createRequest()
                );

        assertThat(result.getRequestId()).isNotBlank();
        assertThat(result.getCandidates()).hasSize(2);

        assertThat(result.getCandidates().get(0).getRouteId())
                .isEqualTo("walking-1");

        assertThat(result.getCandidates().get(0).getRouteType())
                .isEqualTo(RouteType.WALKING);

        assertThat(result.getCandidates().get(1).getRouteId())
                .isEqualTo("transit-1");

        assertThat(result.getCandidates().get(1).getRouteType())
                .isEqualTo(RouteType.TRANSIT);
    }

    @Test
    @DisplayName("보행 경로 조회가 실패해도 대중교통 경로를 반환한다")
    void createCandidatesWhenWalkingFails() {
        when(walkingRouteCandidateService.createCandidate(any()))
                .thenThrow(
                        new CustomException(
                                ErrorCode.ROUTE_SEARCH_FAILED
                        )
                );

        when(routeCandidateService.createCandidates(any()))
                .thenReturn(
                        RouteCandidateResult.builder()
                                .requestId("transit-request")
                                .candidates(
                                        List.of(
                                                createTransitCandidate()
                                        )
                                )
                                .build()
                );

        RouteCandidateResult result =
                routeCandidateAggregationService.createCandidates(
                        createRequest()
                );

        assertThat(result.getCandidates()).hasSize(1);
        assertThat(result.getCandidates().get(0).getRouteType())
                .isEqualTo(RouteType.TRANSIT);
    }

    @Test
    @DisplayName("대중교통 경로 조회가 실패해도 보행 경로를 반환한다")
    void createCandidatesWhenTransitFails() {
        when(walkingRouteCandidateService.createCandidate(any()))
                .thenReturn(createWalkingCandidate());

        when(routeCandidateService.createCandidates(any()))
                .thenThrow(
                        new CustomException(
                                ErrorCode.ROUTE_SEARCH_FAILED
                        )
                );

        RouteCandidateResult result =
                routeCandidateAggregationService.createCandidates(
                        createRequest()
                );

        assertThat(result.getCandidates()).hasSize(1);
        assertThat(result.getCandidates().get(0).getRouteType())
                .isEqualTo(RouteType.WALKING);
    }

    @Test
    @DisplayName("보행과 대중교통 경로 조회가 모두 실패하면 예외가 발생한다")
    void createCandidatesWhenAllRoutesFail() {
        when(walkingRouteCandidateService.createCandidate(any()))
                .thenThrow(
                        new CustomException(
                                ErrorCode.ROUTE_SEARCH_FAILED
                        )
                );

        when(routeCandidateService.createCandidates(any()))
                .thenThrow(
                        new CustomException(
                                ErrorCode.ROUTE_SEARCH_FAILED
                        )
                );

        assertThatThrownBy(() ->
                routeCandidateAggregationService.createCandidates(
                        createRequest()
                )
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception ->
                        assertThat(
                                ((CustomException) exception)
                                        .getErrorCode()
                        ).isEqualTo(
                                ErrorCode.ROUTE_SEARCH_FAILED
                        )
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

    private RouteCandidate createWalkingCandidate() {
        return RouteCandidate.builder()
                .routeId("walking-1")
                .routeType(RouteType.WALKING)
                .providerRank(1)
                .metrics(
                        RouteMetrics.builder()
                                .totalTimeSec(1800)
                                .totalWalkTimeSec(1800)
                                .totalWalkDistanceM(1500)
                                .transferCount(0)
                                .build()
                )
                .build();
    }

    private RouteCandidate createTransitCandidate() {
        return RouteCandidate.builder()
                .routeId("transit-1")
                .routeType(RouteType.TRANSIT)
                .providerRank(1)
                .metrics(
                        RouteMetrics.builder()
                                .totalTimeSec(1500)
                                .totalWalkTimeSec(420)
                                .totalWalkDistanceM(350)
                                .transferCount(1)
                                .build()
                )
                .build();
    }
}