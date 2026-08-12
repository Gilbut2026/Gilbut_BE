package com.gilbeot.gilbut.dto.route.walking.response;

import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RestStopRerouteItemResponse {

    private String routeId;
    private WalkingRouteOption routeOption;
    private SelectedRestStopResponse restStop;
    private WalkingRouteSummaryResponse summary;
    private List<RoutePointResponse> routePoints;
    private List<WalkingStepResponse> steps;
    private List<RestStopRerouteSegmentResponse> segments;
}
