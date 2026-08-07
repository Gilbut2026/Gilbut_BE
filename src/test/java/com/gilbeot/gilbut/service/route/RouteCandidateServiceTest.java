package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.client.tmap.TmapTransitRouteClient;
import com.gilbeot.gilbut.client.tmap.dto.transit.TmapTransitRouteRequest;
import com.gilbeot.gilbut.client.tmap.dto.transit.TmapTransitRouteResponse;
import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
import com.gilbeot.gilbut.dto.route.RouteCandidateResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteCandidateServiceTest {

    @Mock
    private TmapTransitRouteClient tmapTransitRouteClient;

    private RouteCandidateService routeCandidateService;

    @BeforeEach
    void setUp() {
        routeCandidateService =
                new RouteCandidateService(
                        tmapTransitRouteClient
                );
    }

    @Test
    @DisplayName(
            "TMAP 대중교통 경로를 경로 후보로 변환한다"
    )
    void createCandidates() {
        TmapTransitRouteResponse.Itinerary first =
                new TmapTransitRouteResponse.Itinerary();

        first.setTotalTime(2400);
        first.setTotalWalkTime(720);
        first.setTotalWalkDistance(850);
        first.setTransferCount(1);

        TmapTransitRouteResponse.Itinerary second =
                new TmapTransitRouteResponse.Itinerary();

        second.setTotalTime(2600);
        second.setTotalWalkTime(500);
        second.setTotalWalkDistance(600);
        second.setTransferCount(2);

        TmapTransitRouteResponse.Plan plan =
                new TmapTransitRouteResponse.Plan();

        plan.setItineraries(
                List.of(first, second)
        );

        TmapTransitRouteResponse.MetaData metaData =
                new TmapTransitRouteResponse.MetaData();

        metaData.setPlan(plan);

        TmapTransitRouteResponse response =
                new TmapTransitRouteResponse();

        response.setMetaData(metaData);

        when(
                tmapTransitRouteClient.search(any())
        ).thenReturn(response);

        RouteCandidateRequest request =
                RouteCandidateRequest.builder()
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

        RouteCandidateResult result =
                routeCandidateService.createCandidates(
                        request
                );

        assertThat(result.getRequestId())
                .isNotBlank();

        assertThat(result.getCandidates())
                .hasSize(2);

        assertThat(
                result.getCandidates()
                        .get(0)
                        .getRouteId()
        ).isEqualTo("route-1");

        assertThat(
                result.getCandidates()
                        .get(0)
                        .getProviderRank()
        ).isEqualTo(1);

        assertThat(
                result.getCandidates()
                        .get(0)
                        .getMetrics()
                        .getTotalTimeSec()
        ).isEqualTo(2400);

        assertThat(
                result.getCandidates()
                        .get(0)
                        .getMetrics()
                        .getTotalWalkTimeSec()
        ).isEqualTo(720);

        assertThat(
                result.getCandidates()
                        .get(0)
                        .getMetrics()
                        .getTotalWalkDistanceM()
        ).isEqualTo(850);

        assertThat(
                result.getCandidates()
                        .get(0)
                        .getMetrics()
                        .getTransferCount()
        ).isEqualTo(1);

        ArgumentCaptor<TmapTransitRouteRequest> captor =
                ArgumentCaptor.forClass(
                        TmapTransitRouteRequest.class
                );

        org.mockito.Mockito.verify(
                tmapTransitRouteClient
        ).search(captor.capture());

        TmapTransitRouteRequest tmapRequest =
                captor.getValue();

        assertThat(
                tmapRequest.getStartX()
        ).isEqualTo("127.0286");

        assertThat(
                tmapRequest.getStartY()
        ).isEqualTo("37.2636");

        assertThat(
                tmapRequest.getEndX()
        ).isEqualTo("127.047");

        assertThat(
                tmapRequest.getEndY()
        ).isEqualTo("37.279");

        assertThat(
                tmapRequest.getSearchDttm()
        ).isEqualTo("202608081000");

        assertThat(
                tmapRequest.getCount()
        ).isEqualTo(5);
    }
}