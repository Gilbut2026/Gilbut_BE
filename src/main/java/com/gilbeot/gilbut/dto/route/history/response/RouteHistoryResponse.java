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
public class RouteHistoryResponse {

    private Long historyId;
    private String originName;
    private String destinationName;
    private String recommendedRouteId;
    private RouteType recommendedRouteType;
    private WalkingRouteOption recommendedRouteOption;
    private Integer totalTimeSec;
    private Integer totalWalkTimeSec;
    private Integer totalWalkDistanceM;
    private Integer transferCount;
    private boolean drtRecommended;
    private DrtServiceArea drtServiceArea;
    private LocalDateTime createdAt;

    public static RouteHistoryResponse from(
            RouteHistory routeHistory
    ) {
        return RouteHistoryResponse.builder()
                .historyId(routeHistory.getId())
                .originName(routeHistory.getOriginName())
                .destinationName(routeHistory.getDestinationName())
                .recommendedRouteId(
                        routeHistory.getRecommendedRouteId()
                )
                .recommendedRouteType(
                        routeHistory.getRecommendedRouteType()
                )
                .recommendedRouteOption(
                        routeHistory.getRecommendedRouteOption()
                )
                .totalTimeSec(routeHistory.getTotalTimeSec())
                .totalWalkTimeSec(
                        routeHistory.getTotalWalkTimeSec()
                )
                .totalWalkDistanceM(
                        routeHistory.getTotalWalkDistanceM()
                )
                .transferCount(routeHistory.getTransferCount())
                .drtRecommended(routeHistory.isDrtRecommended())
                .drtServiceArea(routeHistory.getDrtServiceArea())
                .createdAt(routeHistory.getCreatedAt())
                .build();
    }
}