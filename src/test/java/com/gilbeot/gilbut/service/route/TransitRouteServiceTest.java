package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.client.tmap.TmapTransitRouteClient;
import com.gilbeot.gilbut.client.tmap.dto.transit.TmapTransitRouteRequest;
import com.gilbeot.gilbut.client.tmap.dto.transit.TmapTransitRouteResponse;
import com.gilbeot.gilbut.dto.route.TransitRouteFailureCode;
import com.gilbeot.gilbut.dto.route.transit.request.TransitRouteRequest;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.global.exception.TransitRouteSearchException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransitRouteServiceTest {

    @Mock
    private TmapTransitRouteClient tmapTransitRouteClient;

    private TransitRouteService transitRouteService;

    @BeforeEach
    void setUp() {
        transitRouteService =
                new TransitRouteService(
                        tmapTransitRouteClient
                );
    }

    @Test
    @DisplayName("TMAP 대중교통 응답을 대중교통 경로 조회 응답으로 변환한다")
    void searchTransitRoute() {
        when(
                tmapTransitRouteClient.search(any())
        ).thenReturn(tmapResponse());

        TransitRouteResponse response =
                transitRouteService.search(
                        transitRouteRequest()
                );

        assertThat(response.getRoutes())
                .hasSize(2);

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getRouteId()
        ).startsWith("transit-");

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getProviderRank()
        ).isEqualTo(1);

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getSummary()
                        .getTotalTimeSec()
        ).isEqualTo(2400);

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getSummary()
                        .getTotalWalkTimeSec()
        ).isEqualTo(720);

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getSummary()
                        .getTotalWalkDistanceM()
        ).isEqualTo(850);

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getSummary()
                        .getTransferCount()
        ).isEqualTo(1);

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getSummary()
                        .getTotalDistanceM()
        ).isEqualTo(3200);

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getSummary()
                        .getFareKrw()
        ).isEqualTo(1500);

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getSummary()
                        .getPathType()
        ).isEqualTo(2);

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getRoutePoints()
        ).hasSize(4);

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getRoutePoints()
                        .get(0)
                        .getLatitude()
        ).isEqualTo(37.2636);

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getRoutePoints()
                        .get(0)
                        .getLongitude()
        ).isEqualTo(127.0286);

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getLegs()
        ).hasSize(2);

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getLegs()
                        .get(0)
                        .getMode()
        ).isEqualTo("WALK");

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getLegs()
                        .get(0)
                        .getSteps()
        ).hasSize(1);

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getLegs()
                        .get(0)
                        .getSteps()
                        .get(0)
                        .getInstruction()
        ).isEqualTo("100m 이동");

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getLegs()
                        .get(1)
                        .getMode()
        ).isEqualTo("BUS");

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getLegs()
                        .get(1)
                        .getRouteName()
        ).isEqualTo("간선:400");

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getLegs()
                        .get(1)
                        .getRouteColor()
        ).isEqualTo("0068B7");

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getLegs()
                        .get(1)
                        .getProviderRouteId()
        ).isEqualTo("11504001");

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getLegs()
                        .get(1)
                        .getVehicleType()
        ).isEqualTo(11);

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getLegs()
                        .get(1)
                        .getServiceAvailable()
        ).isTrue();

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getLegs()
                        .get(1)
                        .getStationCount()
        ).isEqualTo(2);

        assertThat(
                response.getRoutes()
                        .get(0)
                        .getLegs()
                        .get(1)
                        .getStops()
        ).hasSize(2);

        ArgumentCaptor<TmapTransitRouteRequest> captor =
                ArgumentCaptor.forClass(
                        TmapTransitRouteRequest.class
                );

        verify(
                tmapTransitRouteClient
        ).search(captor.capture());

        TmapTransitRouteRequest tmapRequest =
                captor.getValue();

        assertThat(tmapRequest.getStartX())
                .isEqualTo("127.0286");

        assertThat(tmapRequest.getStartY())
                .isEqualTo("37.2636");

        assertThat(tmapRequest.getEndX())
                .isEqualTo("127.047");

        assertThat(tmapRequest.getEndY())
                .isEqualTo("37.279");

        assertThat(tmapRequest.getSearchDttm())
                .isEqualTo("202608081000");

        assertThat(tmapRequest.getCount())
                .isEqualTo(5);

        assertThat(tmapRequest.getLang())
                .isZero();

        assertThat(tmapRequest.getFormat())
                .isEqualTo("json");
    }

    @Test
    @DisplayName("출발 시간이 없으면 INVALID_REQUEST로 처리한다")
    void missingDepartureDateTime() {
        TransitRouteRequest request =
                transitRouteRequest();

        request.setDepartureDateTime(null);

        assertThatThrownBy(() ->
                transitRouteService.search(request)
        )
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(tmapTransitRouteClient);
    }

    @Test
    @DisplayName("유효하지 않은 좌표 요청은 INVALID_REQUEST로 처리한다")
    void invalidCoordinate() {
        TransitRouteRequest request =
                transitRouteRequest();

        request.getOrigin()
                .setLatitude(0.0);
        request.getOrigin()
                .setLongitude(0.0);

        assertThatThrownBy(() ->
                transitRouteService.search(request)
        )
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(tmapTransitRouteClient);
    }

    @Test
    @DisplayName("TMAP 응답에 경로 후보가 없으면 ROUTE_SEARCH_FAILED로 처리한다")
    void emptyItineraries() {
        when(
                tmapTransitRouteClient.search(any())
        ).thenReturn(new TmapTransitRouteResponse());

        Throwable throwable =
                catchThrowable(() ->
                        transitRouteService.search(
                                transitRouteRequest()
                        )
                );

        assertThat(throwable)
                .isInstanceOf(TransitRouteSearchException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROUTE_SEARCH_FAILED);

        assertThat(throwable)
                .isInstanceOf(TransitRouteSearchException.class)
                .extracting("failureCode")
                .isEqualTo(
                        TransitRouteFailureCode.NO_ROUTE
                );
    }

    private TransitRouteRequest transitRouteRequest() {
        TransitRouteRequest.RoutePlaceRequest origin =
                new TransitRouteRequest.RoutePlaceRequest();

        origin.setPlaceId("origin-1");
        origin.setName("수원역");
        origin.setAddress("경기도 수원시 팔달구 덕영대로 924");
        origin.setLatitude(37.2636);
        origin.setLongitude(127.0286);

        TransitRouteRequest.RoutePlaceRequest destination =
                new TransitRouteRequest.RoutePlaceRequest();

        destination.setPlaceId("destination-1");
        destination.setName("광교중앙역");
        destination.setAddress("경기도 수원시 영통구 도청로 45");
        destination.setLatitude(37.279);
        destination.setLongitude(127.047);

        TransitRouteRequest request =
                new TransitRouteRequest();

        request.setOrigin(origin);
        request.setDestination(destination);
        request.setDepartureDateTime(
                LocalDateTime.of(
                        2026,
                        8,
                        8,
                        10,
                        0
                )
        );

        return request;
    }

    private TmapTransitRouteResponse tmapResponse() {
        TmapTransitRouteResponse.Itinerary first =
                new TmapTransitRouteResponse.Itinerary();

        first.setTotalTime(2400);
        first.setTotalWalkTime(720);
        first.setTotalWalkDistance(850);
        first.setTotalDistance(3200);
        first.setTransferCount(1);
        first.setPathType(2);
        first.setFare(fare(1500));
        first.setLegs(
                List.of(
                        walkLeg(),
                        busLeg()
                )
        );

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

        return response;
    }

    private TmapTransitRouteResponse.Fare fare(
            int totalFare
    ) {
        TmapTransitRouteResponse.Regular regular =
                new TmapTransitRouteResponse.Regular();

        regular.setTotalFare(totalFare);

        TmapTransitRouteResponse.Fare fare =
                new TmapTransitRouteResponse.Fare();

        fare.setRegular(regular);

        return fare;
    }

    private TmapTransitRouteResponse.Leg walkLeg() {
        TmapTransitRouteResponse.WalkingStep step =
                new TmapTransitRouteResponse.WalkingStep();

        step.setDistance(100);
        step.setStreetName("덕영대로");
        step.setDescription("100m 이동");
        step.setLinestring(
                "127.0286,37.2636 127.0290,37.2640"
        );

        TmapTransitRouteResponse.Leg leg =
                new TmapTransitRouteResponse.Leg();

        leg.setMode("WALK");
        leg.setDistance(100);
        leg.setSectionTime(120);
        leg.setStart(
                stopPoint(
                        "출발지",
                        37.2636,
                        127.0286
                )
        );
        leg.setEnd(
                stopPoint(
                        "수원역",
                        37.264,
                        127.029
                )
        );
        leg.setSteps(
                List.of(step)
        );

        return leg;
    }

    private TmapTransitRouteResponse.Leg busLeg() {
        TmapTransitRouteResponse.PassShape passShape =
                new TmapTransitRouteResponse.PassShape();

        passShape.setLinestring(
                "127.0290,37.2640 127.0350,37.2700 127.0470,37.2790"
        );

        TmapTransitRouteResponse.PassStopList passStopList =
                new TmapTransitRouteResponse.PassStopList();

        passStopList.setStationList(
                List.of(
                        station(
                                0,
                                "772608",
                                "수원역",
                                37.264,
                                127.029
                        ),
                        station(
                                1,
                                "772411",
                                "광교중앙역",
                                37.279,
                                127.047
                        )
                )
        );

        TmapTransitRouteResponse.Leg leg =
                new TmapTransitRouteResponse.Leg();

        leg.setMode("BUS");
        leg.setRoute("간선:400");
        leg.setRouteColor("0068B7");
        leg.setRouteId("11504001");
        leg.setType(11);
        leg.setService(1);
        leg.setDistance(3100);
        leg.setSectionTime(2280);
        leg.setStart(
                stopPoint(
                        "수원역",
                        37.264,
                        127.029
                )
        );
        leg.setEnd(
                stopPoint(
                        "광교중앙역",
                        37.279,
                        127.047
                )
        );
        leg.setPassShape(passShape);
        leg.setPassStopList(passStopList);

        return leg;
    }

    private TmapTransitRouteResponse.StopPoint stopPoint(
            String name,
            double latitude,
            double longitude
    ) {
        TmapTransitRouteResponse.StopPoint point =
                new TmapTransitRouteResponse.StopPoint();

        point.setName(name);
        point.setLat(latitude);
        point.setLon(longitude);

        return point;
    }

    private TmapTransitRouteResponse.Station station(
            int index,
            String stationId,
            String name,
            double latitude,
            double longitude
    ) {
        TmapTransitRouteResponse.Station station =
                new TmapTransitRouteResponse.Station();

        station.setIndex(index);
        station.setStationID(stationId);
        station.setStationName(name);
        station.setLat(String.valueOf(latitude));
        station.setLon(String.valueOf(longitude));

        return station;
    }
}
