package com.gilbeot.gilbut.dto.route.history.response;

import com.gilbeot.gilbut.domain.history.RouteHistory;
import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import com.gilbeot.gilbut.dto.drt.DrtServiceArea;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RouteHistoryDetailResponse {

    private Long historyId;
    private String requestId;
    private PlaceResponse origin;
    private PlaceResponse destination;
    private LocalDateTime departureDateTime;
    private String recommendedRouteId;
    private String recommendationReason;
    private RouteType recommendedRouteType;
    private WalkingRouteOption recommendedRouteOption;
    private Integer totalTimeSec;
    private Integer totalWalkTimeSec;
    private Integer totalWalkDistanceM;
    private Integer transferCount;
    private boolean drtRecommended;
    private DrtServiceArea drtServiceArea;
    private LocalDateTime createdAt;

    public static RouteHistoryDetailResponse from(
            RouteHistory routeHistory
    ) {
        return RouteHistoryDetailResponse.builder()
                .historyId(
                        routeHistory.getId()
                )
                .requestId(
                        routeHistory.getRequestId()
                )
                .origin(
                        PlaceResponse.builder()
                                .name(
                                        routeHistory.getOriginName()
                                )
                                .address(
                                        routeHistory.getOriginAddress()
                                )
                                .latitude(
                                        routeHistory.getOriginLatitude()
                                )
                                .longitude(
                                        routeHistory.getOriginLongitude()
                                )
                                .build()
                )
                .destination(
                        PlaceResponse.builder()
                                .name(
                                        routeHistory.getDestinationName()
                                )
                                .address(
                                        routeHistory.getDestinationAddress()
                                )
                                .latitude(
                                        routeHistory.getDestinationLatitude()
                                )
                                .longitude(
                                        routeHistory.getDestinationLongitude()
                                )
                                .build()
                )
                .departureDateTime(
                        routeHistory.getDepartureDateTime()
                )
                .recommendedRouteId(
                        routeHistory.getRecommendedRouteId()
                )
                .recommendationReason(
                        routeHistory.getRecommendationReason()
                )
                .recommendedRouteType(
                        routeHistory.getRecommendedRouteType()
                )
                .recommendedRouteOption(
                        routeHistory.getRecommendedRouteOption()
                )
                .totalTimeSec(
                        routeHistory.getTotalTimeSec()
                )
                .totalWalkTimeSec(
                        routeHistory.getTotalWalkTimeSec()
                )
                .totalWalkDistanceM(
                        routeHistory.getTotalWalkDistanceM()
                )
                .transferCount(
                        routeHistory.getTransferCount()
                )
                .drtRecommended(
                        routeHistory.isDrtRecommended()
                )
                .drtServiceArea(
                        routeHistory.getDrtServiceArea()
                )
                .createdAt(
                        routeHistory.getCreatedAt()
                )
                .build();
    }

    @Getter
    @Builder
    public static class PlaceResponse {

        private String name;
        private String address;
        private Double latitude;
        private Double longitude;
    }
}