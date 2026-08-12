package com.gilbeot.gilbut.dto.route;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(toBuilder = true)
public class RouteCandidate {

    private String routeId;
    private RouteType routeType;
    private WalkingRouteOption routeOption;
    private Integer providerRank;
    private RouteMetrics metrics;

    @JsonIgnore
    private List<RouteWalkSegment> walkSegments;
}