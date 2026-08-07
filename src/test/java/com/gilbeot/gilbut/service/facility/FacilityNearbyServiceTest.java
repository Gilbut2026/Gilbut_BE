package com.gilbeot.gilbut.service.facility;

import com.gilbeot.gilbut.domain.facility.FacilityType;
import com.gilbeot.gilbut.dto.facility.request.NearbyFacilityRequest;
import com.gilbeot.gilbut.dto.facility.response.FacilityItemResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FacilityNearbyServiceTest {

    private FacilityNearbyService facilityNearbyService;

    @BeforeEach
    void setUp() {
        FacilityCsvService facilityCsvService =
                new FacilityCsvService();
        facilityCsvService.loadFacilities();

        facilityNearbyService =
                new FacilityNearbyService(facilityCsvService);
    }

    @Test
    void findsNearbyFacilitiesSortedByDistance() {
        List<FacilityItemResponse> items =
                facilityNearbyService.findNearby(
                        request(
                                "37.2894164551",
                                "127.0570407171",
                                null,
                                null
                        )
                ).getItems();

        assertThat(items).isNotEmpty();
        assertThat(items)
                .extracting(FacilityItemResponse::getDistanceMeters)
                .isSorted();
    }

    @Test
    void filtersByType() {
        List<FacilityItemResponse> items =
                facilityNearbyService.findNearby(
                        request(
                                "37.2894164551",
                                "127.0570407171",
                                "1000",
                                "TOILET"
                        )
                ).getItems();

        assertThat(items).isNotEmpty();
        assertThat(items)
                .extracting(FacilityItemResponse::getType)
                .containsOnly(FacilityType.TOILET);
    }

    @Test
    void rejectsRadiusOverMaximum() {
        assertInvalidRequest(() ->
                facilityNearbyService.findNearby(
                        request(
                                "37.2894164551",
                                "127.0570407171",
                                "5001",
                                null
                        )
                )
        );
    }

    @Test
    void rejectsUnknownType() {
        assertInvalidRequest(() ->
                facilityNearbyService.findNearby(
                        request(
                                "37.2894164551",
                                "127.0570407171",
                                "1000",
                                "PARK"
                        )
                )
        );
    }

    private NearbyFacilityRequest request(
            String lat,
            String lng,
            String radiusMeters,
            String types
    ) {
        NearbyFacilityRequest request =
                new NearbyFacilityRequest();
        request.setLat(lat);
        request.setLng(lng);
        request.setRadiusMeters(radiusMeters);
        request.setTypes(types);

        return request;
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
