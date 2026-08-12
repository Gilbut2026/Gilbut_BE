package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import com.gilbeot.gilbut.dto.route.walking.request.NavigationRerouteRequest;
import com.gilbeot.gilbut.dto.route.walking.request.WalkingRouteRequest;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteResponse;
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
}
