package com.gilbeot.gilbut.client.ai.dto.scoring;

import com.gilbeot.gilbut.client.ai.dto.scoring.type.DrtReasonCode;
import com.gilbeot.gilbut.client.ai.dto.scoring.type.FilterCode;
import com.gilbeot.gilbut.client.ai.dto.scoring.type.ScoringResultStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRouteScoringResponse {

    private String requestId;
    private String scoringVersion;
    private List<Result> results;
    private DrtDecision drtDecision;
    private ScoringError error;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {

        private String routeId;
        private ScoringResultStatus status;
        private Double score;
        private Integer rank;
        private List<FilterCode> filterCodes;
        private ScoreBreakdown scoreBreakdown;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreBreakdown {

        private Double walkTimePenalty;
        private Double walkDistancePenalty;
        private Double obstaclePenalty;
        private Double transferPenalty;
        private Double weatherPenalty;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DrtDecision {

        private Boolean show;
        private Boolean priority;
        private Boolean taxiGuide;
        private List<DrtReasonCode> reasonCodes;
        private String basedOnRouteId;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoringError {

        private String code;
        private String message;
        private Boolean retryable;
    }
}