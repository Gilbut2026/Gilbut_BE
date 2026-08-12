package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import com.gilbeot.gilbut.dto.route.RouteAccessibilitySignals;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteItemResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteSummaryResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteItemResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RouteCandidateMapperTest {

    private final RouteCandidateMapper routeCandidateMapper =
            new RouteCandidateMapper();

    @Test
    @DisplayName("보행 경로의 routeOption과 접근성 신호를 경로 후보에 유지한다")
    void keepsWalkingRouteOptionAndAccessibilitySignals() {

        WalkingRouteResponse response =
                WalkingRouteResponse.builder()
                        .routes(
                                List.of(
                                        walkingRoute(
                                                "walking-1",
                                                WalkingRouteOption.DEFAULT
                                        ),
                                        walkingRoute(
                                                "walking-avoid-stairs-1",
                                                WalkingRouteOption.AVOID_STAIRS
                                        )
                                )
                        )
                        .build();

        List<RouteCandidate> candidates =
                routeCandidateMapper.fromWalkingRoutes(response);

        assertThat(candidates)
                .extracting(RouteCandidate::getRouteOption)
                .containsExactly(
                        WalkingRouteOption.DEFAULT,
                        WalkingRouteOption.AVOID_STAIRS
                );

        RouteCandidate defaultCandidate =
                candidates.get(0);

        assertThat(defaultCandidate.getWalkSegments())
                .hasSize(1);

        assertThat(
                defaultCandidate.getWalkSegments()
                        .get(0)
                        .getAccessibilitySignals()
                        .getStair()
                        .getState()
        ).isEqualTo(
                RouteAccessibilitySignals.State.PRESENT
        );

        assertThat(
                defaultCandidate.getWalkSegments()
                        .get(0)
                        .getAccessibilitySignals()
                        .getStair()
                        .getCount()
        ).isEqualTo(1);

        assertThat(
                defaultCandidate.getWalkSegments()
                        .get(0)
                        .getAccessibilitySignals()
                        .getOverpass()
                        .getState()
        ).isEqualTo(
                RouteAccessibilitySignals.State.ABSENT
        );

        assertThat(
                defaultCandidate.getWalkSegments()
                        .get(0)
                        .getAccessibilitySignals()
                        .getUnderpass()
                        .getState()
        ).isEqualTo(
                RouteAccessibilitySignals.State.ABSENT
        );
    }

    @Test
    @DisplayName("대중교통 경로 후보의 routeOption은 null이다")
    void leavesTransitRouteOptionNull() {

        TransitRouteResponse response =
                TransitRouteResponse.builder()
                        .routes(
                                List.of(
                                        TransitRouteItemResponse.builder()
                                                .routeId("transit-1")
                                                .providerRank(1)
                                                .summary(
                                                        TransitRouteSummaryResponse.builder()
                                                                .totalTimeSec(1200)
                                                                .totalWalkTimeSec(300)
                                                                .totalWalkDistanceM(250)
                                                                .transferCount(1)
                                                                .build()
                                                )
                                                .routePoints(List.of())
                                                .legs(List.of())
                                                .build()
                                )
                        )
                        .build();

        List<RouteCandidate> candidates =
                routeCandidateMapper.fromTransitRoutes(response);

        assertThat(candidates)
                .hasSize(1);

        assertThat(candidates.get(0).getRouteType())
                .isEqualTo(RouteType.TRANSIT);

        assertThat(candidates.get(0).getRouteOption())
                .isNull();
    }

    private WalkingRouteItemResponse walkingRoute(
            String routeId,
            WalkingRouteOption routeOption
    ) {
        return WalkingRouteItemResponse.builder()
                .routeId(routeId)
                .routeOption(routeOption)
                .summary(
                        WalkingRouteSummaryResponse.builder()
                                .totalDistanceM(1000)
                                .totalTimeSec(900)
                                .build()
                )
                .routePoints(List.of())
                .steps(List.of())
                .accessibilitySignals(
                        RouteAccessibilitySignals.known(
                                1,
                                0,
                                0
                        )
                )
                .build();
    }
}