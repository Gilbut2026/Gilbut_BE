package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteMetrics;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteItemResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteSummaryResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteSummaryResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteCandidateMapper {

    public RouteCandidate fromWalkingRoute(
            WalkingRouteResponse response
    ) {
        if (response == null
                || response.getRouteId() == null
                || response.getSummary() == null) {
            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }

        WalkingRouteSummaryResponse summary =
                response.getSummary();

        if (summary.getTotalTimeSec() == null
                || summary.getTotalDistanceM() == null) {
            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }

        return RouteCandidate.builder()
                .routeId(response.getRouteId())
                .routeType(RouteType.WALKING)
                .providerRank(1)
                .metrics(
                        RouteMetrics.builder()
                                .totalTimeSec(
                                        summary.getTotalTimeSec()
                                )
                                .totalWalkTimeSec(
                                        summary.getTotalTimeSec()
                                )
                                .totalWalkDistanceM(
                                        summary.getTotalDistanceM()
                                )
                                .transferCount(0)
                                .build()
                )
                .build();
    }

    public List<RouteCandidate> fromTransitRoutes(
            TransitRouteResponse response
    ) {
        if (response == null
                || response.getRoutes() == null
                || response.getRoutes().isEmpty()) {
            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }

        return response.getRoutes()
                .stream()
                .map(this::fromTransitRoute)
                .toList();
    }

    private RouteCandidate fromTransitRoute(
            TransitRouteItemResponse route
    ) {
        if (route == null
                || route.getRouteId() == null
                || route.getSummary() == null) {
            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }

        TransitRouteSummaryResponse summary =
                route.getSummary();

        if (summary.getTotalTimeSec() == null
                || summary.getTotalWalkTimeSec() == null
                || summary.getTotalWalkDistanceM() == null
                || summary.getTransferCount() == null) {
            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }

        return RouteCandidate.builder()
                .routeId(route.getRouteId())
                .routeType(RouteType.TRANSIT)
                .providerRank(route.getProviderRank())
                .metrics(
                        RouteMetrics.builder()
                                .totalTimeSec(
                                        summary.getTotalTimeSec()
                                )
                                .totalWalkTimeSec(
                                        summary.getTotalWalkTimeSec()
                                )
                                .totalWalkDistanceM(
                                        summary.getTotalWalkDistanceM()
                                )
                                .transferCount(
                                        summary.getTransferCount()
                                )
                                .build()
                )
                .build();
    }
}