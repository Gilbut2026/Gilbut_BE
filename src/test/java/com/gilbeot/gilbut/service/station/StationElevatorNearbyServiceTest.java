package com.gilbeot.gilbut.service.station;

import com.gilbeot.gilbut.client.gg.GyeonggiElevatorClient;
import com.gilbeot.gilbut.client.tmap.TmapStationClient;
import com.gilbeot.gilbut.domain.station.StationElevator;
import com.gilbeot.gilbut.domain.station.TransitStation;
import com.gilbeot.gilbut.dto.station.request.NearbyStationElevatorRequest;
import com.gilbeot.gilbut.dto.station.response.NearbyStationElevatorResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationElevatorNearbyServiceTest {

    @Mock
    private TmapStationClient tmapStationClient;

    @Mock
    private GyeonggiElevatorClient gyeonggiElevatorClient;

    @InjectMocks
    private StationElevatorNearbyService
            stationElevatorNearbyService;

    @Test
    void usesDefaultRadiusAndMatchesElevatorsByStationName() {
        when(
                tmapStationClient.searchNearbyStations(
                        37.265,
                        127.001,
                        3000
                )
        ).thenReturn(
                List.of(
                        station("far", "수원역 1호선", "수원", 300),
                        station("near", "수원역", "수원", 100)
                )
        );
        when(gyeonggiElevatorClient.getElevators())
                .thenReturn(
                        List.of(
                                elevator("수원", "1호선")
                        )
                );

        NearbyStationElevatorResponse response =
                stationElevatorNearbyService.findNearby(
                        request(
                                "37.265",
                                "127.001",
                                null
                        )
                );

        assertThat(response.getStations()).hasSize(1);
        assertThat(response.getStations().get(0).getStationId())
                .isEqualTo("near");
        assertThat(response.getStations().get(0).getElevatorCount())
                .isEqualTo(1);
        assertThat(
                response.getStations()
                        .get(0)
                        .getElevators()
                        .get(0)
                        .getRouteName()
        ).isEqualTo("1호선");

        verify(tmapStationClient)
                .searchNearbyStations(
                        37.265,
                        127.001,
                        3000
                );
    }

    @Test
    void rejectsRadiusOverMaximum() {
        assertInvalidRequest(() ->
                stationElevatorNearbyService.findNearby(
                        request(
                                "37.265",
                                "127.001",
                                "20001"
                        )
                )
        );
    }

    private NearbyStationElevatorRequest request(
            String lat,
            String lng,
            String radiusMeters
    ) {
        NearbyStationElevatorRequest request =
                new NearbyStationElevatorRequest();
        request.setLat(lat);
        request.setLng(lng);
        request.setRadiusMeters(radiusMeters);

        return request;
    }

    private TransitStation station(
            String id,
            String name,
            String normalizedName,
            int distanceMeters
    ) {
        return TransitStation.builder()
                .stationId(id)
                .name(name)
                .normalizedName(normalizedName)
                .address("경기도 수원시")
                .latitude(37.265)
                .longitude(127.001)
                .distanceMeters(distanceMeters)
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
