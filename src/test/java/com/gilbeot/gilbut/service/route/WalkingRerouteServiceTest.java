package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.domain.route.RestStopRerouteSegmentType;
import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import com.gilbeot.gilbut.dto.route.walking.request.NavigationRerouteRequest;
import com.gilbeot.gilbut.dto.route.walking.request.RestStopRerouteRequest;
import com.gilbeot.gilbut.dto.route.walking.request.WalkingRouteRequest;
import com.gilbeot.gilbut.dto.route.walking.response.RestStopRerouteItemResponse;
import com.gilbeot.gilbut.dto.route.walking.response.RestStopRerouteResponse;
import com.gilbeot.gilbut.dto.route.walking.response.RoutePointResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteItemResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteSummaryResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingStepResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class WalkingRerouteServiceTest {

    @Mock
    private WalkingRouteService walkingRouteService;

    @Test
    @DisplayName("현재 위치를 출발지로 변환하여 보행 경로를 재탐색한다")
    void rerouteWalkingRoute() {
        WalkingRerouteService walkingRerouteService =
                new WalkingRerouteService(walkingRouteService);

        WalkingRouteResponse expectedResponse =
                WalkingRouteResponse.builder()
                        .routes(List.of())
                        .build();

        when(
                walkingRouteService.search(
                        org.mockito.ArgumentMatchers.any()
                )
        ).thenReturn(expectedResponse);

        WalkingRouteResponse response =
                walkingRerouteService.reroute(
                        rerouteRequest()
                );

        assertThat(response)
                .isSameAs(expectedResponse);

        ArgumentCaptor<WalkingRouteRequest> captor =
                ArgumentCaptor.forClass(
                        WalkingRouteRequest.class
                );

        verify(walkingRouteService)
                .search(captor.capture());

        WalkingRouteRequest walkingRequest =
                captor.getValue();

        assertThat(
                walkingRequest.getOrigin()
                        .getName()
        ).isEqualTo("현재 위치");

        assertThat(
                walkingRequest.getOrigin()
                        .getLatitude()
        ).isEqualTo(37.2636);

        assertThat(
                walkingRequest.getOrigin()
                        .getLongitude()
        ).isEqualTo(127.0286);

        assertThat(
                walkingRequest.getDestination()
                        .getName()
        ).isEqualTo("광교중앙역");

        assertThat(
                walkingRequest.getDestination()
                        .getLatitude()
        ).isEqualTo(37.279);

        assertThat(
                walkingRequest.getDestination()
                        .getLongitude()
        ).isEqualTo(127.047);

        assertThat(walkingRequest.getRouteOptions())
                .containsExactly(
                        WalkingRouteOption.AVOID_STAIRS
                );
    }

    @Test
    @DisplayName("재탐색 요청이 없으면 INVALID_REQUEST로 처리한다")
    void nullRequest() {
        WalkingRerouteService walkingRerouteService =
                new WalkingRerouteService(walkingRouteService);

        assertThatThrownBy(() ->
                walkingRerouteService.reroute(null)
        )
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(walkingRouteService);
    }

    @Test
    @DisplayName("선택한 쉼터를 경유하는 보행 경로를 재탐색한다")
    void rerouteViaRestStop() {
        WalkingRerouteService walkingRerouteService =
                new WalkingRerouteService(walkingRouteService);

        when(
                walkingRouteService.search(
                        org.mockito.ArgumentMatchers.any()
                )
        ).thenReturn(
                walkingResponse(
                        routeItem(
                                WalkingRouteOption.DEFAULT,
                                100,
                                120,
                                List.of(
                                        point(37.2636, 127.0286),
                                        point(37.2701, 127.0352)
                                ),
                                "쉼터까지 이동"
                        ),
                        routeItem(
                                WalkingRouteOption.AVOID_STAIRS,
                                130,
                                150,
                                List.of(
                                        point(37.2636, 127.0286),
                                        point(37.2701, 127.0352)
                                ),
                                "계단 회피 경로로 쉼터까지 이동"
                        )
                ),
                walkingResponse(
                        routeItem(
                                WalkingRouteOption.DEFAULT,
                                200,
                                240,
                                List.of(
                                        point(37.2701, 127.0352),
                                        point(37.279, 127.047)
                                ),
                                "목적지까지 이동"
                        ),
                        routeItem(
                                WalkingRouteOption.AVOID_STAIRS,
                                260,
                                300,
                                List.of(
                                        point(37.2701, 127.0352),
                                        point(37.279, 127.047)
                                ),
                                "계단 회피 경로로 목적지까지 이동"
                        )
                )
        );

        RestStopRerouteResponse response =
                walkingRerouteService.rerouteViaRestStop(
                        restStopRerouteRequest()
                );

        assertThat(response.getRoutes())
                .hasSize(2);

        RestStopRerouteItemResponse defaultRoute =
                response.getRoutes().get(0);

        assertThat(defaultRoute.getRouteId())
                .startsWith("walking-rest-stop-");

        assertThat(defaultRoute.getRouteOption())
                .isEqualTo(WalkingRouteOption.DEFAULT);

        assertThat(
                defaultRoute.getRestStop()
                        .getFacilityId()
        ).isEqualTo("shelter-1");

        assertThat(
                defaultRoute.getRestStop()
                        .getName()
        ).isEqualTo("매탄공원 쉼터");

        assertThat(
                defaultRoute.getSummary()
                        .getTotalDistanceM()
        ).isEqualTo(300);

        assertThat(
                defaultRoute.getSummary()
                        .getTotalTimeSec()
        ).isEqualTo(360);

        assertThat(defaultRoute.getRoutePoints())
                .hasSize(3);

        assertThat(defaultRoute.getSteps())
                .extracting(WalkingStepResponse::getStepIndex)
                .containsExactly(1, 2);

        assertThat(defaultRoute.getSegments())
                .extracting(
                        segment -> segment.getSegmentType()
                )
                .containsExactly(
                        RestStopRerouteSegmentType
                                .CURRENT_LOCATION_TO_REST_STOP,
                        RestStopRerouteSegmentType
                                .REST_STOP_TO_DESTINATION
                );

        RestStopRerouteItemResponse avoidStairsRoute =
                response.getRoutes().get(1);

        assertThat(avoidStairsRoute.getRouteId())
                .startsWith(
                        "walking-rest-stop-avoid-stairs-"
                );

        assertThat(avoidStairsRoute.getRouteOption())
                .isEqualTo(WalkingRouteOption.AVOID_STAIRS);

        assertThat(
                avoidStairsRoute.getSummary()
                        .getTotalDistanceM()
        ).isEqualTo(390);

        ArgumentCaptor<WalkingRouteRequest> captor =
                ArgumentCaptor.forClass(
                        WalkingRouteRequest.class
                );

        verify(
                walkingRouteService,
                times(2)
        ).search(captor.capture());

        WalkingRouteRequest firstRequest =
                captor.getAllValues().get(0);

        assertThat(
                firstRequest.getOrigin()
                        .getName()
        ).isEqualTo("현재 위치");

        assertThat(
                firstRequest.getDestination()
                        .getPlaceId()
        ).isEqualTo("shelter-1");

        assertThat(
                firstRequest.getDestination()
                        .getName()
        ).isEqualTo("매탄공원 쉼터");

        WalkingRouteRequest secondRequest =
                captor.getAllValues().get(1);

        assertThat(
                secondRequest.getOrigin()
                        .getPlaceId()
        ).isEqualTo("shelter-1");

        assertThat(
                secondRequest.getDestination()
                        .getName()
        ).isEqualTo("광교중앙역");

        assertThat(firstRequest.getRouteOptions())
                .containsExactly(
                        WalkingRouteOption.DEFAULT,
                        WalkingRouteOption.AVOID_STAIRS
                );

        assertThat(secondRequest.getRouteOptions())
                .containsExactly(
                        WalkingRouteOption.DEFAULT,
                        WalkingRouteOption.AVOID_STAIRS
                );
    }

    @Test
    @DisplayName("두 구간에서 공통으로 성공한 경로 옵션이 없으면 ROUTE_SEARCH_FAILED로 처리한다")
    void rerouteViaRestStopWithoutMatchedRouteOption() {
        WalkingRerouteService walkingRerouteService =
                new WalkingRerouteService(walkingRouteService);

        when(
                walkingRouteService.search(
                        org.mockito.ArgumentMatchers.any()
                )
        ).thenReturn(
                walkingResponse(
                        routeItem(
                                WalkingRouteOption.DEFAULT,
                                100,
                                120,
                                List.of(
                                        point(37.2636, 127.0286),
                                        point(37.2701, 127.0352)
                                ),
                                "쉼터까지 이동"
                        )
                ),
                walkingResponse(
                        routeItem(
                                WalkingRouteOption.AVOID_STAIRS,
                                260,
                                300,
                                List.of(
                                        point(37.2701, 127.0352),
                                        point(37.279, 127.047)
                                ),
                                "계단 회피 경로로 목적지까지 이동"
                        )
                )
        );

        assertThatThrownBy(() ->
                walkingRerouteService.rerouteViaRestStop(
                        restStopRerouteRequest()
                )
        )
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROUTE_SEARCH_FAILED);
    }

    @Test
    @DisplayName("쉼터 경유 재탐색 요청이 없으면 INVALID_REQUEST로 처리한다")
    void nullRestStopRerouteRequest() {
        WalkingRerouteService walkingRerouteService =
                new WalkingRerouteService(walkingRouteService);

        assertThatThrownBy(() ->
                walkingRerouteService.rerouteViaRestStop(null)
        )
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(walkingRouteService);
    }

    private NavigationRerouteRequest rerouteRequest() {
        NavigationRerouteRequest.RoutePlaceRequest
                currentLocation =
                new NavigationRerouteRequest.RoutePlaceRequest();

        currentLocation.setPlaceId("current-1");
        currentLocation.setName("현재 위치");
        currentLocation.setAddress("경기도 수원시 팔달구 덕영대로 924");
        currentLocation.setLatitude(37.2636);
        currentLocation.setLongitude(127.0286);

        NavigationRerouteRequest.RoutePlaceRequest
                destination =
                new NavigationRerouteRequest.RoutePlaceRequest();

        destination.setPlaceId("destination-1");
        destination.setName("광교중앙역");
        destination.setAddress("경기도 수원시 영통구 도청로 45");
        destination.setLatitude(37.279);
        destination.setLongitude(127.047);

        NavigationRerouteRequest request =
                new NavigationRerouteRequest();

        request.setCurrentLocation(currentLocation);
        request.setDestination(destination);
        request.setRouteOptions(
                List.of(WalkingRouteOption.AVOID_STAIRS)
        );

        return request;
    }

    private RestStopRerouteRequest restStopRerouteRequest() {
        RestStopRerouteRequest.RoutePlaceRequest
                currentLocation =
                new RestStopRerouteRequest.RoutePlaceRequest();

        currentLocation.setPlaceId("current-1");
        currentLocation.setName("현재 위치");
        currentLocation.setAddress("경기도 수원시 팔달구 덕영대로 924");
        currentLocation.setLatitude(37.2636);
        currentLocation.setLongitude(127.0286);

        RestStopRerouteRequest.RestStopRequest restStop =
                new RestStopRerouteRequest.RestStopRequest();

        restStop.setFacilityId("shelter-1");
        restStop.setName("매탄공원 쉼터");
        restStop.setAddress("경기도 수원시 영통구 매탄동");
        restStop.setLatitude(37.2701);
        restStop.setLongitude(127.0352);

        RestStopRerouteRequest.RoutePlaceRequest destination =
                new RestStopRerouteRequest.RoutePlaceRequest();

        destination.setPlaceId("destination-1");
        destination.setName("광교중앙역");
        destination.setAddress("경기도 수원시 영통구 도청로 45");
        destination.setLatitude(37.279);
        destination.setLongitude(127.047);

        RestStopRerouteRequest request =
                new RestStopRerouteRequest();

        request.setCurrentLocation(currentLocation);
        request.setRestStop(restStop);
        request.setDestination(destination);
        request.setRouteOptions(
                List.of(
                        WalkingRouteOption.DEFAULT,
                        WalkingRouteOption.AVOID_STAIRS
                )
        );

        return request;
    }

    private WalkingRouteResponse walkingResponse(
            WalkingRouteItemResponse... routes
    ) {
        return WalkingRouteResponse.builder()
                .routes(List.of(routes))
                .build();
    }

    private WalkingRouteItemResponse routeItem(
            WalkingRouteOption routeOption,
            int distanceM,
            int timeSec,
            List<RoutePointResponse> routePoints,
            String instruction
    ) {
        return WalkingRouteItemResponse.builder()
                .routeId("route-id")
                .routeOption(routeOption)
                .summary(
                        WalkingRouteSummaryResponse.builder()
                                .totalDistanceM(distanceM)
                                .totalTimeSec(timeSec)
                                .build()
                )
                .routePoints(routePoints)
                .steps(
                        List.of(
                                WalkingStepResponse.builder()
                                        .stepIndex(1)
                                        .instruction(instruction)
                                        .distanceM(distanceM)
                                        .durationSec(timeSec)
                                        .points(routePoints)
                                        .build()
                        )
                )
                .build();
    }

    private RoutePointResponse point(
            double latitude,
            double longitude
    ) {
        return RoutePointResponse.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
