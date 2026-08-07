package com.gilbeot.gilbut.service.station;

import com.gilbeot.gilbut.client.gg.GyeonggiElevatorClient;
import com.gilbeot.gilbut.client.tmap.TmapStationClient;
import com.gilbeot.gilbut.domain.station.StationElevator;
import com.gilbeot.gilbut.domain.station.TransitStation;
import com.gilbeot.gilbut.dto.station.request.AlongRouteStationElevatorRequest;
import com.gilbeot.gilbut.dto.station.response.AlongRouteStationElevatorItemResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationElevatorAlongRouteServiceTest {

    @Mock
    private TmapStationClient tmapStationClient;

    @Mock
    private GyeonggiElevatorClient gyeonggiElevatorClient;

    private StationElevatorAlongRouteService
            stationElevatorAlongRouteService;

    @BeforeEach
    void setUp() {
        stationElevatorAlongRouteService =
                new StationElevatorAlongRouteService(
                        tmapStationClient,
                        gyeonggiElevatorClient
                );
    }

    @Test
    @DisplayName("경로 주변 역을 가까운 순서로 조회하고 엘리베이터를 매칭한다")
    void findsStationsAlongRouteSortedByDistance() {
        when(
                tmapStationClient.searchNearbyStations(
                        37.0,
                        127.0,
                        100
                )
        ).thenReturn(
                List.of(
                        station(
                                "suwon",
                                "수원역",
                                "수원",
                                37.0,
                                127.005
                        ),
                        station(
                                "outside",
                                "경로 밖 역",
                                "경로밖",
                                37.002,
                                127.005
                        )
                )
        );
        when(
                tmapStationClient.searchNearbyStations(
                        37.0,
                        127.01,
                        100
                )
        ).thenReturn(
                List.of(
                        station(
                                "maetan",
                                "매탄권선역",
                                "매탄권선",
                                37.0004,
                                127.006
                        )
                )
        );
        when(gyeonggiElevatorClient.getElevators())
                .thenReturn(
                        List.of(
                                elevator("수원", "1호선")
                        )
                );

        List<AlongRouteStationElevatorItemResponse> stations =
                stationElevatorAlongRouteService.findAlongRoute(
                        request(
                                100,
                                List.of(
                                        point(37.0, 127.0),
                                        point(37.0, 127.01)
                                )
                        )
                ).getStations();

        assertThat(stations)
                .hasSize(2);
        assertThat(stations)
                .extracting(
                        AlongRouteStationElevatorItemResponse
                                ::getDistanceFromRouteM
                )
                .isSorted();
        assertThat(stations)
                .extracting(
                        AlongRouteStationElevatorItemResponse
                                ::getStationName
                )
                .containsExactly(
                        "수원역",
                        "매탄권선역"
                );

        assertThat(stations.get(0).getElevatorCount())
                .isEqualTo(1);
        assertThat(stations.get(0).getElevators())
                .hasSize(1);
        assertThat(
                stations.get(0)
                        .getElevators()
                        .get(0)
                        .getRouteName()
        ).isEqualTo("1호선");

        assertThat(stations.get(1).getElevatorCount())
                .isZero();
        assertThat(stations.get(1).getElevators())
                .isEmpty();
    }

    @Test
    @DisplayName("routePoints가 많으면 TMAP 역 검색 호출 지점을 30개로 제한한다")
    void limitsTmapSearchPoints() {
        when(
                tmapStationClient.searchNearbyStations(
                        anyDouble(),
                        anyDouble(),
                        anyInt()
                )
        ).thenReturn(List.of());

        stationElevatorAlongRouteService.findAlongRoute(
                request(
                        null,
                        createRoutePoints(1000)
                )
        );

        verify(tmapStationClient, times(30))
                .searchNearbyStations(
                        anyDouble(),
                        anyDouble(),
                        anyInt()
                );
        verifyNoInteractions(gyeonggiElevatorClient);
    }

    @Test
    @DisplayName("반경이 최대값을 초과하면 INVALID_REQUEST로 처리한다")
    void rejectsRadiusOverMaximum() {
        assertInvalidRequest(() ->
                stationElevatorAlongRouteService.findAlongRoute(
                        request(
                                1001,
                                List.of(
                                        point(37.0, 127.0),
                                        point(37.0, 127.01)
                                )
                        )
                )
        );

        verifyNoInteractions(tmapStationClient);
        verifyNoInteractions(gyeonggiElevatorClient);
    }

    @Test
    @DisplayName("경로 좌표가 1000개를 초과하면 INVALID_REQUEST로 처리한다")
    void rejectsTooManyRoutePoints() {
        assertInvalidRequest(() ->
                stationElevatorAlongRouteService.findAlongRoute(
                        request(
                                300,
                                createRoutePoints(1001)
                        )
                )
        );

        verifyNoInteractions(tmapStationClient);
        verifyNoInteractions(gyeonggiElevatorClient);
    }

    @Test
    @DisplayName("0,0 좌표는 INVALID_REQUEST로 처리한다")
    void rejectsZeroCoordinate() {
        assertInvalidRequest(() ->
                stationElevatorAlongRouteService.findAlongRoute(
                        request(
                                300,
                                List.of(
                                        point(0, 0),
                                        point(37.0, 127.01)
                                )
                        )
                )
        );

        verifyNoInteractions(tmapStationClient);
        verifyNoInteractions(gyeonggiElevatorClient);
    }

    private AlongRouteStationElevatorRequest request(
            Integer radiusMeters,
            List<AlongRouteStationElevatorRequest.RoutePointRequest>
                    routePoints
    ) {
        AlongRouteStationElevatorRequest request =
                new AlongRouteStationElevatorRequest();

        request.setRadiusMeters(radiusMeters);
        request.setRoutePoints(routePoints);

        return request;
    }

    private List<AlongRouteStationElevatorRequest.RoutePointRequest>
    createRoutePoints(
            int count
    ) {
        List<AlongRouteStationElevatorRequest.RoutePointRequest>
                routePoints = new ArrayList<>();

        for (int index = 0; index < count; index++) {
            routePoints.add(
                    point(
                            37.0,
                            127.0 + index * 0.00001
                    )
            );
        }

        return routePoints;
    }

    private AlongRouteStationElevatorRequest.RoutePointRequest point(
            double latitude,
            double longitude
    ) {
        AlongRouteStationElevatorRequest.RoutePointRequest point =
                new AlongRouteStationElevatorRequest.RoutePointRequest();

        point.setLatitude(latitude);
        point.setLongitude(longitude);

        return point;
    }

    private TransitStation station(
            String id,
            String name,
            String normalizedName,
            double latitude,
            double longitude
    ) {
        return TransitStation.builder()
                .stationId(id)
                .name(name)
                .normalizedName(normalizedName)
                .address("경기도 수원시")
                .latitude(latitude)
                .longitude(longitude)
                .distanceMeters(0)
                .build();
    }

    private StationElevator elevator(
            String normalizedStationName,
            String routeName
    ) {
        return StationElevator.builder()
                .stationName(normalizedStationName + "역")
                .normalizedStationName(normalizedStationName)
                .routeName(routeName)
                .operator("한국철도공사")
                .exitNumber("1")
                .location("1번 출구")
                .floorRange("B1 -> 1F")
                .state("운행")
                .elevatorNumber("ELV-1")
                .capacityCount("15")
                .capacityWeight("1000")
                .build();
    }

    private void assertInvalidRequest(
            Runnable action
    ) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(
                        CustomException.class,
                        exception ->
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(
                                                ErrorCode.INVALID_REQUEST
                                        )
                );
    }
}
