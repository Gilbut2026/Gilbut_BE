package com.gilbeot.gilbut.dto.route.walking.response;

import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WalkingRouteItemResponse {

    private String routeId;
    private WalkingRouteOption routeOption;
    private WalkingRouteSummaryResponse summary;
    private List<RoutePointResponse> routePoints;
    private List<WalkingStepResponse> steps;
}
