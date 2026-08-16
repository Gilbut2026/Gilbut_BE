package com.gilbeot.gilbut.dto.route;

import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RouteCandidateResult {

    private String requestId;
    private List<RouteCandidate> candidates;
    private WalkingRouteResponse walkingRoute;
    private TransitRouteResponse transitRoutes;
    private TransitRouteFailure transitRouteFailure;
}
