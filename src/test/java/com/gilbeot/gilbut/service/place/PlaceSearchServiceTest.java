package com.gilbeot.gilbut.service.place;

import com.gilbeot.gilbut.dto.place.request.PlaceSearchRequest;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaceSearchServiceTest {

    private final PlaceSearchService placeSearchService =
            new PlaceSearchService();

    @Test
    void rejectsBlankKeyword() {
        assertInvalidRequest(() ->
                placeSearchService.search(
                        request(" ", null, null, null, null, null)
                )
        );
    }

    @Test
    void rejectsKeywordLongerThan100Characters() {
        assertInvalidRequest(() ->
                placeSearchService.search(
                        request("a".repeat(101), null, null, null, null, null)
                )
        );
    }

    @Test
    void rejectsSingleCoordinate() {
        assertInvalidRequest(() ->
                placeSearchService.search(
                        request("광교중앙역", "37.288", null, null, null, null)
                )
        );
    }

    @Test
    void rejectsRadiusWithoutCoordinates() {
        assertInvalidRequest(() ->
                placeSearchService.search(
                        request("광교중앙역", null, null, "5", null, null)
                )
        );
    }

    @Test
    void rejectsRadiusOutOfRange() {
        assertInvalidRequest(() ->
                placeSearchService.search(
                        request("광교중앙역", "37.288", "127.051", "34", null, null)
                )
        );
    }

    @Test
    void rejectsInvalidPageAndSize() {
        assertInvalidRequest(() ->
                placeSearchService.search(
                        request("광교중앙역", null, null, null, "0", "10")
                )
        );

        assertInvalidRequest(() ->
                placeSearchService.search(
                        request("광교중앙역", null, null, null, "1", "51")
                )
        );
    }

    private PlaceSearchRequest request(
            String keyword,
            String lat,
            String lon,
            String radiusKm,
            String page,
            String size
    ) {
        PlaceSearchRequest request = new PlaceSearchRequest();
        request.setKeyword(keyword);
        request.setLat(lat);
        request.setLon(lon);
        request.setRadiusKm(radiusKm);
        request.setPage(page);
        request.setSize(size);

        return request;
    }

    private void assertInvalidRequest(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(
                        CustomException.class,
                        exception ->
                                org.assertj.core.api.Assertions
                                        .assertThat(exception.getErrorCode())
                                        .isEqualTo(ErrorCode.INVALID_REQUEST)
                );
    }
}
