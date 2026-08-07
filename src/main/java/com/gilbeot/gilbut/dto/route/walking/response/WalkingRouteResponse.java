package com.gilbeot.gilbut.dto.route.walking.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WalkingRouteResponse {

    private String routeId;
    private WalkingRouteSummaryResponse summary;
    private List<RoutePointResponse> routePoints;
    private List<WalkingStepResponse> steps;
}
