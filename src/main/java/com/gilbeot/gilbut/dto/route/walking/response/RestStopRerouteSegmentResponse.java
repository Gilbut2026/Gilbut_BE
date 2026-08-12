package com.gilbeot.gilbut.dto.route.walking.response;

import com.gilbeot.gilbut.domain.route.RestStopRerouteSegmentType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RestStopRerouteSegmentResponse {

    private RestStopRerouteSegmentType segmentType;
    private WalkingRouteSummaryResponse summary;
    private List<RoutePointResponse> routePoints;
    private List<WalkingStepResponse> steps;
}
