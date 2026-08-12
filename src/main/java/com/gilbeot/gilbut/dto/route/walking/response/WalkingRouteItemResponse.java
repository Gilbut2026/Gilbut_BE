package com.gilbeot.gilbut.dto.route.walking.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import com.gilbeot.gilbut.dto.route.RouteAccessibilitySignals;
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

    @JsonIgnore
    private RouteAccessibilitySignals accessibilitySignals;
}