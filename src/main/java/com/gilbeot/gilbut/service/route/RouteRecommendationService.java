package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.client.ai.AiRouteScoringClient;
import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringRequest;
import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringResponse;
import com.gilbeot.gilbut.client.ai.dto.scoring.type.ScoringResultStatus;
import com.gilbeot.gilbut.client.ai.mapper.AiRouteScoringRequestMapper;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
import com.gilbeot.gilbut.dto.route.RouteCandidateResult;
import com.gilbeot.gilbut.dto.route.RouteRecommendationItem;
import com.gilbeot.gilbut.dto.route.RouteRecommendationResult;
import com.gilbeot.gilbut.dto.user.response.MobilityProfileResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.service.user.UserMobilityProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RouteRecommendationService {

    private final RouteCandidateAggregationService routeCandidateAggregationService;
    private final UserMobilityProfileService userMobilityProfileService;
    private final AiRouteScoringRequestMapper aiRouteScoringRequestMapper;
    private final AiRouteScoringClient aiRouteScoringClient;

    public RouteRecommendationResult recommend(
            Long userId,
            RouteCandidateRequest request
    ) {
        RouteCandidateResult candidateResult =
                routeCandidateAggregationService.createCandidates(
                        request
                );

        MobilityProfileResponse mobilityProfile =
                userMobilityProfileService.getMobilityProfile(
                        userId
                );

        AiRouteScoringRequest scoringRequest =
                aiRouteScoringRequestMapper.toRequest(
                        mobilityProfile,
                        candidateResult
                );

        AiRouteScoringResponse scoringResponse =
                aiRouteScoringClient.score(
                        scoringRequest
                );

        validateResponse(
                candidateResult,
                scoringResponse
        );

        return buildResult(
                candidateResult,
                scoringResponse
        );
    }

    private void validateResponse(
            RouteCandidateResult candidateResult,
            AiRouteScoringResponse scoringResponse
    ) {
        if (scoringResponse == null
                || scoringResponse.getRequestId() == null
                || !candidateResult.getRequestId()
                .equals(scoringResponse.getRequestId())) {
            throw new CustomException(
                    ErrorCode.AI_ROUTE_SCORING_RESPONSE_INVALID
            );
        }

        if (scoringResponse.getError() != null) {
            throw new CustomException(
                    ErrorCode.AI_ROUTE_SCORING_FAILED
            );
        }

        if (scoringResponse.getResults() == null) {
            throw new CustomException(
                    ErrorCode.AI_ROUTE_SCORING_RESPONSE_INVALID
            );
        }

        Set<String> candidateRouteIds =
                candidateResult.getCandidates()
                        .stream()
                        .map(RouteCandidate::getRouteId)
                        .collect(HashSet::new, Set::add, Set::addAll);

        Set<String> responseRouteIds =
                new HashSet<>();

        for (AiRouteScoringResponse.Result result
                : scoringResponse.getResults()) {

            if (result == null
                    || result.getRouteId() == null
                    || result.getStatus() == null
                    || !candidateRouteIds.contains(
                    result.getRouteId()
            )
                    || !responseRouteIds.add(
                    result.getRouteId()
            )) {
                throw new CustomException(
                        ErrorCode.AI_ROUTE_SCORING_RESPONSE_INVALID
                );
            }

            if (result.getStatus()
                    == ScoringResultStatus.SCORED
                    && (result.getScore() == null
                    || result.getRank() == null)) {
                throw new CustomException(
                        ErrorCode.AI_ROUTE_SCORING_RESPONSE_INVALID
                );
            }
        }

        if (!candidateRouteIds.equals(
                responseRouteIds
        )) {
            throw new CustomException(
                    ErrorCode.AI_ROUTE_SCORING_RESPONSE_INVALID
            );
        }

        if (scoringResponse.getDrtDecision() != null
                && scoringResponse.getDrtDecision()
                .getBasedOnRouteId() != null
                && !candidateRouteIds.contains(
                scoringResponse.getDrtDecision()
                        .getBasedOnRouteId()
        )) {
            throw new CustomException(
                    ErrorCode.AI_ROUTE_SCORING_RESPONSE_INVALID
            );
        }
    }

    private RouteRecommendationResult buildResult(
            RouteCandidateResult candidateResult,
            AiRouteScoringResponse scoringResponse
    ) {
        Map<String, RouteCandidate> candidateMap =
                new HashMap<>();

        for (RouteCandidate candidate
                : candidateResult.getCandidates()) {
            candidateMap.put(
                    candidate.getRouteId(),
                    candidate
            );
        }

        List<RouteRecommendationItem> recommendations =
                scoringResponse.getResults()
                        .stream()
                        .filter(result ->
                                result.getStatus()
                                        == ScoringResultStatus.SCORED
                        )
                        .sorted((first, second) ->
                                Integer.compare(
                                        first.getRank(),
                                        second.getRank()
                                )
                        )
                        .map(result ->
                                RouteRecommendationItem.builder()
                                        .routeId(
                                                result.getRouteId()
                                        )
                                        .candidate(
                                                candidateMap.get(
                                                        result.getRouteId()
                                                )
                                        )
                                        .score(
                                                result.getScore()
                                        )
                                        .rank(
                                                result.getRank()
                                        )
                                        .scoreBreakdown(
                                                result.getScoreBreakdown()
                                        )
                                        .slopeSummary(
                                                result.getSlopeSummary()
                                        )
                                        .build()
                        )
                        .toList();

        List<AiRouteScoringResponse.Result> filteredResults =
                scoringResponse.getResults()
                        .stream()
                        .filter(result ->
                                result.getStatus()
                                        == ScoringResultStatus.FILTERED
                        )
                        .toList();

        return RouteRecommendationResult.builder()
                .requestId(
                        candidateResult.getRequestId()
                )
                .scoringVersion(
                        scoringResponse.getScoringVersion()
                )
                .recommendations(
                        recommendations
                )
                .filteredResults(
                        filteredResults
                )
                .drtDecision(
                        scoringResponse.getDrtDecision()
                )
                .walkingRoute(
                        candidateResult.getWalkingRoute()
                )
                .transitRoutes(
                        candidateResult.getTransitRoutes()
                )
                .build();
    }
}
