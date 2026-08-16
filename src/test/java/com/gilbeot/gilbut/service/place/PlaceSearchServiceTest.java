package com.gilbeot.gilbut.service.place;

import com.gilbeot.gilbut.dto.place.request.PlaceSearchRequest;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PlaceSearchServiceTest {

    private final PlaceSearchService placeSearchService =
            new PlaceSearchService();

    private static final String EMPTY_TMAP_RESPONSE =
            """
            {
              "searchPoiInfo": {
                "totalCount": "0",
                "pois": {
                  "poi": []
                }
              }
            }
            """;

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

    @Test
    void rejectsInvalidSort() {
        assertInvalidRequest(() ->
                placeSearchService.search(
                        request(
                                "광교중앙역",
                                null,
                                null,
                                null,
                                null,
                                null,
                                "popular"
                        )
                )
        );
    }

    @Test
    void rejectsDistanceSortWithoutCoordinates() {
        assertInvalidRequest(() ->
                placeSearchService.search(
                        request(
                                "광교중앙역",
                                null,
                                null,
                                null,
                                null,
                                null,
                                "distance"
                        )
                )
        );
    }

    @Test
    void defaultsToAccuracySortWithoutCoordinates() {
        MockRestServiceServer server =
                mockTmapServer(
                        uri -> {
                            String query = uri.getRawQuery();

                            assertThat(query)
                                    .contains(
                                            "searchtypCd=A",
                                            "count=20"
                                    );

                            assertThat(query)
                                    .doesNotContain(
                                            "centerLat",
                                            "centerLon",
                                            "radius"
                                    );
                        }
                );

        placeSearchService.search(
                request(
                        "광교중앙역",
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );

        server.verify();
    }

    @Test
    void defaultsToDistanceSortWithCoordinates() {
        MockRestServiceServer server =
                mockTmapServer(
                        uri -> {
                            String query = uri.getRawQuery();

                            assertThat(query)
                                    .contains(
                                            "searchtypCd=R",
                                            "centerLat=37.288",
                                            "centerLon=127.051",
                                            "radius=5",
                                            "count=20"
                                    );
                        }
                );

        placeSearchService.search(
                request(
                        "광교중앙역",
                        "37.288",
                        "127.051",
                        null,
                        null,
                        null
                )
        );

        server.verify();
    }

    @Test
    void usesAccuracySortWithCoordinatesWhenRequested() {
        MockRestServiceServer server =
                mockTmapServer(
                        uri -> {
                            String query = uri.getRawQuery();

                            assertThat(query)
                                    .contains(
                                            "searchtypCd=A",
                                            "centerLat=37.288",
                                            "centerLon=127.051",
                                            "radius=7",
                                            "count=20"
                                    );
                        }
                );

        placeSearchService.search(
                request(
                        "광교중앙역",
                        "37.288",
                        "127.051",
                        "7",
                        null,
                        null,
                        "accuracy"
                )
        );

        server.verify();
    }

    @Test
    void usesDistanceSortWithCoordinatesWhenRequested() {
        MockRestServiceServer server =
                mockTmapServer(
                        uri -> {
                            String query = uri.getRawQuery();

                            assertThat(query)
                                    .contains(
                                            "searchtypCd=R",
                                            "centerLat=37.288",
                                            "centerLon=127.051",
                                            "radius=7",
                                            "count=30"
                                    );
                        }
                );

        placeSearchService.search(
                request(
                        "병원",
                        "37.288",
                        "127.051",
                        "7",
                        null,
                        "30",
                        "distance"
                )
        );

        server.verify();
    }

    private PlaceSearchRequest request(
            String keyword,
            String lat,
            String lon,
            String radiusKm,
            String page,
            String size
    ) {
        return request(
                keyword,
                lat,
                lon,
                radiusKm,
                page,
                size,
                null
        );
    }

    private PlaceSearchRequest request(
            String keyword,
            String lat,
            String lon,
            String radiusKm,
            String page,
            String size,
            String sort
    ) {
        PlaceSearchRequest request = new PlaceSearchRequest();
        request.setKeyword(keyword);
        request.setLat(lat);
        request.setLon(lon);
        request.setRadiusKm(radiusKm);
        request.setPage(page);
        request.setSize(size);
        request.setSort(sort);

        return request;
    }

    private MockRestServiceServer mockTmapServer(
            java.util.function.Consumer<URI> uriVerifier
    ) {
        RestTemplate restTemplate =
                (RestTemplate) ReflectionTestUtils.getField(
                        placeSearchService,
                        "restTemplate"
                );

        ReflectionTestUtils.setField(
                placeSearchService,
                "tmapAppKey",
                "test-key"
        );

        MockRestServiceServer server =
                MockRestServiceServer
                        .bindTo(restTemplate)
                        .build();

        server.expect(request -> {
            assertThat(
                    request.getHeaders()
                            .getFirst("appKey")
            ).isEqualTo("test-key");

            uriVerifier.accept(
                    request.getURI()
            );
        }).andRespond(
                withSuccess(
                        EMPTY_TMAP_RESPONSE,
                        MediaType.APPLICATION_JSON
                )
        );

        return server;
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
