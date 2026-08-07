package com.gilbeot.gilbut.service.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gilbeot.gilbut.client.tmap.TmapWalkingRouteClient;
import com.gilbeot.gilbut.client.tmap.dto.walking.TmapWalkingRouteRequest;
import com.gilbeot.gilbut.client.tmap.dto.walking.TmapWalkingRouteResponse;
import com.gilbeot.gilbut.dto.route.walking.request.WalkingRouteRequest;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalkingRouteServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TmapWalkingRouteClient tmapWalkingRouteClient;

    private WalkingRouteService walkingRouteService;

    @BeforeEach
    void setUp() {
        walkingRouteService =
                new WalkingRouteService(
                        tmapWalkingRouteClient
                );
    }

    @Test
    @DisplayName("TMAP 보행자 응답을 보행 경로 조회 응답으로 변환한다")
    void searchWalkingRoute() throws Exception {
        when(
                tmapWalkingRouteClient.search(any())
        ).thenReturn(tmapResponse());

        WalkingRouteResponse response =
                walkingRouteService.search(
                        walkingRouteRequest()
                );

        assertThat(response.getRouteId())
                .startsWith("walking-");

        assertThat(response.getSummary().getTotalDistanceM())
                .isEqualTo(70);

        assertThat(response.getSummary().getTotalTimeSec())
                .isEqualTo(80);

        assertThat(response.getRoutePoints())
                .hasSize(3);

        assertThat(
                response.getRoutePoints()
                        .get(0)
                        .getLatitude()
        ).isEqualTo(37.265714);

        assertThat(
                response.getRoutePoints()
                        .get(0)
                        .getLongitude()
        ).isEqualTo(126.999958);

        assertThat(response.getSteps())
                .hasSize(2);

        assertThat(
                response.getSteps()
                        .get(0)
                        .getInstruction()
        ).isEqualTo("출발지");

        assertThat(
                response.getSteps()
                        .get(0)
                        .getDistanceM()
        ).isEqualTo(35);

        assertThat(
                response.getSteps()
                        .get(1)
                        .getInstruction()
        ).isEqualTo("우회전");

        assertThat(
                response.getSteps()
                        .get(1)
                        .getTurnType()
        ).isEqualTo(13);

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

        assertThat(tmapRequest.getStartName())
                .isEqualTo("수원역");

        assertThat(tmapRequest.getEndName())
                .isEqualTo("광교중앙역");
    }

    @Test
    @DisplayName("유효하지 않은 좌표 요청은 INVALID_REQUEST로 처리한다")
    void invalidCoordinate() {
        WalkingRouteRequest request =
                walkingRouteRequest();

        request.getOrigin()
                .setLatitude(0.0);
        request.getOrigin()
                .setLongitude(0.0);

        assertThatThrownBy(() ->
                walkingRouteService.search(request)
        )
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(tmapWalkingRouteClient);
    }

    @Test
    @DisplayName("TMAP 응답에 경로 좌표가 없으면 ROUTE_SEARCH_FAILED로 처리한다")
    void emptyRoutePoints() {
        TmapWalkingRouteResponse.Properties properties =
                new TmapWalkingRouteResponse.Properties();

        properties.setTotalDistance(70);
        properties.setTotalTime(80);

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

        assertThatThrownBy(() ->
                walkingRouteService.search(
                        walkingRouteRequest()
                )
        )
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROUTE_SEARCH_FAILED);
    }

    private WalkingRouteRequest walkingRouteRequest() {
        WalkingRouteRequest.RoutePlaceRequest origin =
                new WalkingRouteRequest.RoutePlaceRequest();

        origin.setPlaceId("origin-1");
        origin.setName("수원역");
        origin.setAddress("경기도 수원시 팔달구 덕영대로 924");
        origin.setLatitude(37.2636);
        origin.setLongitude(127.0286);

        WalkingRouteRequest.RoutePlaceRequest destination =
                new WalkingRouteRequest.RoutePlaceRequest();

        destination.setPlaceId("destination-1");
        destination.setName("광교중앙역");
        destination.setAddress("경기도 수원시 영통구 도청로 45");
        destination.setLatitude(37.279);
        destination.setLongitude(127.047);

        WalkingRouteRequest request =
                new WalkingRouteRequest();

        request.setOrigin(origin);
        request.setDestination(destination);

        return request;
    }

    private TmapWalkingRouteResponse tmapResponse()
            throws Exception {
        TmapWalkingRouteResponse response =
                new TmapWalkingRouteResponse();

        response.setFeatures(
                List.of(
                        pointFeature(
                                70,
                                80,
                                "출발지",
                                200,
                                "SP"
                        ),
                        lineFeature(
                                35,
                                40,
                                """
                                        [
                                          [126.999958, 37.265714],
                                          [126.999785, 37.266003]
                                        ]
                                        """
                        ),
                        pointFeature(
                                null,
                                null,
                                "우회전",
                                13,
                                "GP"
                        ),
                        lineFeature(
                                35,
                                40,
                                """
                                        [
                                          [126.999785, 37.266003],
                                          [127.000100, 37.266500]
                                        ]
                                        """
                        )
                )
        );

        return response;
    }

    private TmapWalkingRouteResponse.Feature pointFeature(
            Integer totalDistance,
            Integer totalTime,
            String description,
            Integer turnType,
            String pointType
    ) throws Exception {
        TmapWalkingRouteResponse.Properties properties =
                new TmapWalkingRouteResponse.Properties();

        properties.setTotalDistance(totalDistance);
        properties.setTotalTime(totalTime);
        properties.setDescription(description);
        properties.setTurnType(turnType);
        properties.setPointType(pointType);

        TmapWalkingRouteResponse.Geometry geometry =
                new TmapWalkingRouteResponse.Geometry();

        geometry.setType("Point");
        geometry.setCoordinates(
                objectMapper.readTree(
                        "[126.999958,37.265714]"
                )
        );

        TmapWalkingRouteResponse.Feature feature =
                new TmapWalkingRouteResponse.Feature();

        feature.setGeometry(geometry);
        feature.setProperties(properties);

        return feature;
    }

    private TmapWalkingRouteResponse.Feature lineFeature(
            Integer distance,
            Integer time,
            String coordinates
    ) throws Exception {
        TmapWalkingRouteResponse.Properties properties =
                new TmapWalkingRouteResponse.Properties();

        properties.setDistance(distance);
        properties.setTime(time);

        TmapWalkingRouteResponse.Geometry geometry =
                new TmapWalkingRouteResponse.Geometry();

        geometry.setType("LineString");
        geometry.setCoordinates(
                objectMapper.readTree(coordinates)
        );

        TmapWalkingRouteResponse.Feature feature =
                new TmapWalkingRouteResponse.Feature();

        feature.setGeometry(geometry);
        feature.setProperties(properties);

        return feature;
    }
}
