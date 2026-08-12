package com.gilbeot.gilbut.dto.route;

import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringResponse;
import com.gilbeot.gilbut.dto.drt.DrtGuideResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RouteRecommendationResult {

    private String requestId;
    private String scoringVersion;
    private List<RouteRecommendationItem> recommendations;
    private List<AiRouteScoringResponse.Result> filteredResults;
    private AiRouteScoringResponse.DrtDecision drtDecision;
    private DrtGuideResponse drtGuide;
    private WalkingRouteResponse walkingRoute;
    private TransitRouteResponse transitRoutes;
}