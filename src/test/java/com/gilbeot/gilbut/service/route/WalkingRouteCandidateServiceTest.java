package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.client.tmap.TmapWalkingRouteClient;
import com.gilbeot.gilbut.client.tmap.dto.walking.TmapWalkingRouteRequest;
import com.gilbeot.gilbut.client.tmap.dto.walking.TmapWalkingRouteResponse;
import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalkingRouteCandidateServiceTest {

    @Mock
    private TmapWalkingRouteClient tmapWalkingRouteClient;

    private WalkingRouteCandidateService walkingRouteCandidateService;

    @BeforeEach
    void setUp() {
        walkingRouteCandidateService =
                new WalkingRouteCandidateService(
                        tmapWalkingRouteClient
                );
    }

    @Test
    @DisplayName("TMAP 보행 경로를 보행 경로 후보로 변환한다")
    void createCandidate() {
        TmapWalkingRouteResponse.Properties properties =
                new TmapWalkingRouteResponse.Properties();

        properties.setTotalDistance(720);
        properties.setTotalTime(900);

        TmapWalkingRouteResponse.Feature feature =
                new TmapWalkingRouteResponse.Feature();

        feature.setProperties(properties);

        TmapWalkingRouteResponse response =
                new TmapWalkingRouteResponse();

        response.setFeatures(
                List.of(feature)
        );

        when(
                tmapWalkingRouteClient.search(any())
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

        RouteCandidate candidate =
                walkingRouteCandidateService.createCandidate(
                        request
                );

        assertThat(candidate.getRouteId())
                .isEqualTo("walking-1");

        assertThat(candidate.getRouteType())
                .isEqualTo(RouteType.WALKING);

        assertThat(candidate.getProviderRank())
                .isEqualTo(1);

        assertThat(
                candidate.getMetrics()
                        .getTotalTimeSec()
        ).isEqualTo(900);

        assertThat(
                candidate.getMetrics()
                        .getTotalWalkTimeSec()
        ).isEqualTo(900);

        assertThat(
                candidate.getMetrics()
                        .getTotalWalkDistanceM()
        ).isEqualTo(720);

        assertThat(
                candidate.getMetrics()
                        .getTransferCount()
        ).isZero();

        ArgumentCaptor<TmapWalkingRouteRequest> captor =
                ArgumentCaptor.forClass(
                        TmapWalkingRouteRequest.class
                );

        verify(
                tmapWalkingRouteClient
        ).search(captor.capture());

        TmapWalkingRouteRequest tmapRequest =
                captor.getValue();

        assertThat(tmapRequest.getStartX())
                .isEqualTo(127.0286);

        assertThat(tmapRequest.getStartY())
                .isEqualTo(37.2636);

        assertThat(tmapRequest.getEndX())
                .isEqualTo(127.047);

        assertThat(tmapRequest.getEndY())
                .isEqualTo(37.279);

        assertThat(tmapRequest.getReqCoordType())
                .isEqualTo("WGS84GEO");

        assertThat(tmapRequest.getResCoordType())
                .isEqualTo("WGS84GEO");
    }
}