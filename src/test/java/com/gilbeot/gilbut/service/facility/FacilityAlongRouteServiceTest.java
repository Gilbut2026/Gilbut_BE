package com.gilbeot.gilbut.service.facility;

import com.gilbeot.gilbut.domain.facility.Facility;
import com.gilbeot.gilbut.domain.facility.FacilityType;
import com.gilbeot.gilbut.dto.facility.request.AlongRouteFacilityRequest;
import com.gilbeot.gilbut.dto.facility.response.AlongRouteFacilityItemResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacilityAlongRouteServiceTest {

    @Mock
    private FacilityCsvService facilityCsvService;

    private FacilityAlongRouteService facilityAlongRouteService;

    @BeforeEach
    void setUp() {
        facilityAlongRouteService =
                new FacilityAlongRouteService(
                        facilityCsvService
                );
    }

    @Test
    @DisplayName("경로 주변 시설을 경로와 가까운 순서로 조회한다")
    void findsFacilitiesAlongRouteSortedByDistance() {
        when(
                facilityCsvService.findByTypes(any())
        ).thenReturn(
                List.of(
                        facility(
                                FacilityType.SHELTER,
                                "1",
                                "경로 위 쉼터",
                                37.0,
                                127.005
                        ),
                        facility(
                                FacilityType.TOILET,
                                "2",
                                "경로 주변 화장실",
                                37.0004,
                                127.004
                        ),
                        facility(
                                FacilityType.TOILET,
                                "3",
                                "경로 밖 화장실",
                                37.002,
                                127.005
                        )
                )
        );

        List<AlongRouteFacilityItemResponse> items =
                facilityAlongRouteService.findAlongRoute(
                        request(
                                null,
                                null,
                                List.of(
                                        point(37.0, 127.0),
                                        point(37.0, 127.01)
                                )
                        )
                ).getItems();

        assertThat(items)
                .hasSize(2);

        assertThat(items)
                .extracting(
                        AlongRouteFacilityItemResponse
                                ::getDistanceFromRouteM
                )
                .isSorted();

        assertThat(items)
                .extracting(
                        AlongRouteFacilityItemResponse::getName
                )
                .containsExactly(
                        "경로 위 쉼터",
                        "경로 주변 화장실"
                );
    }

    @Test
    @DisplayName("시설 타입을 지정하면 해당 타입만 조회한다")
    void filtersByTypes() {
        when(
                facilityCsvService.findByTypes(any())
        ).thenReturn(List.of());

        facilityAlongRouteService.findAlongRoute(
                request(
                        100,
                        List.of(FacilityType.TOILET),
                        List.of(
                                point(37.0, 127.0),
                                point(37.0, 127.01)
                        )
                )
        );

        verify(facilityCsvService)
                .findByTypes(
                        eq(
                                EnumSet.of(FacilityType.TOILET)
                        )
                );
    }

    @Test
    @DisplayName("반경이 최대값을 초과하면 INVALID_REQUEST로 처리한다")
    void rejectsRadiusOverMaximum() {
        assertInvalidRequest(() ->
                facilityAlongRouteService.findAlongRoute(
                        request(
                                501,
                                null,
                                List.of(
                                        point(37.0, 127.0),
                                        point(37.0, 127.01)
                                )
                        )
                )
        );

        verifyNoInteractions(facilityCsvService);
    }

    @Test
    @DisplayName("경로 좌표가 1000개를 초과하면 INVALID_REQUEST로 처리한다")
    void rejectsTooManyRoutePoints() {
        List<AlongRouteFacilityRequest.RoutePointRequest> routePoints =
                new ArrayList<>();

        for (int index = 0; index < 1001; index++) {
            routePoints.add(
                    point(
                            37.0,
                            127.0 + index * 0.00001
                    )
            );
        }

        assertInvalidRequest(() ->
                facilityAlongRouteService.findAlongRoute(
                        request(
                                100,
                                null,
                                routePoints
                        )
                )
        );

        verifyNoInteractions(facilityCsvService);
    }

    @Test
    @DisplayName("0,0 좌표는 INVALID_REQUEST로 처리한다")
    void rejectsZeroCoordinate() {
        assertInvalidRequest(() ->
                facilityAlongRouteService.findAlongRoute(
                        request(
                                100,
                                null,
                                List.of(
                                        point(0, 0),
                                        point(37.0, 127.01)
                                )
                        )
                )
        );

        verifyNoInteractions(facilityCsvService);
    }

    private AlongRouteFacilityRequest request(
            Integer radiusMeters,
            List<FacilityType> types,
            List<AlongRouteFacilityRequest.RoutePointRequest>
                    routePoints
    ) {
        AlongRouteFacilityRequest request =
                new AlongRouteFacilityRequest();

        request.setRadiusMeters(radiusMeters);
        request.setTypes(types);
        request.setRoutePoints(routePoints);

        return request;
    }

    private AlongRouteFacilityRequest.RoutePointRequest point(
            double latitude,
            double longitude
    ) {
        AlongRouteFacilityRequest.RoutePointRequest point =
                new AlongRouteFacilityRequest.RoutePointRequest();

        point.setLatitude(latitude);
        point.setLongitude(longitude);

        return point;
    }

    private Facility facility(
            FacilityType type,
            String sourceId,
            String name,
            double latitude,
            double longitude
    ) {
        return Facility.builder()
                .type(type)
                .sourceId(sourceId)
                .name(name)
                .category("공공시설")
                .subcategory("편의시설")
                .address("경기도 수원시")
                .latitude(latitude)
                .longitude(longitude)
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
