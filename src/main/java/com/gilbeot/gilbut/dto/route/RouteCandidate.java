package com.gilbeot.gilbut.dto.route;

import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RouteCandidate {

    private String routeId;
    private RouteType routeType;
    private WalkingRouteOption routeOption;
    private Integer providerRank;
    private RouteMetrics metrics;
}