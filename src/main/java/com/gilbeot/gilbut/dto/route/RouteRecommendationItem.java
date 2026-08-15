package com.gilbeot.gilbut.dto.route;

import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RouteRecommendationItem {

    private String routeId;
    private RouteCandidate candidate;
    private Double score;
    private Integer rank;
    private String recommendationReason;
    private AiRouteScoringResponse.ScoreBreakdown scoreBreakdown;
    private AiRouteScoringResponse.SlopeSummary slopeSummary;
}