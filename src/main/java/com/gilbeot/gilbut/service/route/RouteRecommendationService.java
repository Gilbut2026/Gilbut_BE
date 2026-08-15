package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.client.ai.AiRouteScoringClient;
import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringRequest;
import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringResponse;
import com.gilbeot.gilbut.client.ai.dto.scoring.type.ScoringResultStatus;
import com.gilbeot.gilbut.client.ai.mapper.AiRouteScoringRequestMapper;
import com.gilbeot.gilbut.dto.drt.DrtGuideResponse;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
import com.gilbeot.gilbut.dto.route.RouteCandidateResult;
import com.gilbeot.gilbut.dto.route.RouteRecommendationItem;
import com.gilbeot.gilbut.dto.route.RouteRecommendationResult;
import com.gilbeot.gilbut.dto.user.response.MobilityProfileResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.service.drt.DrtGuideService;
import com.gilbeot.gilbut.service.history.RouteHistoryService;
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
    private final RouteAccessibilityEnrichmentService routeAccessibilityEnrichmentService;
    private final UserMobilityProfileService userMobilityProfileService;
    private final AiRouteScoringRequestMapper aiRouteScoringRequestMapper;
    private final AiRouteScoringClient aiRouteScoringClient;
    private final DrtGuideService drtGuideService;
    private final RouteAccessibilitySummaryMapper routeAccessibilitySummaryMapper;
    private final RouteRecommendationReasonService routeRecommendationReasonService;
    private final RouteHistoryService routeHistoryService;

    public RouteRecommendationResult recommend(
            Long userId,
            RouteCandidateRequest request
    ) {
        RouteCandidateResult candidateResult =
                routeCandidateAggregationService.createCandidates(
                        request
                );

        candidateResult =
                routeAccessibilityEnrichmentService.enrich(
                        candidateResult
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

        DrtGuideResponse drtGuide =
                drtGuideService.createGuide(
                        request,
                        scoringResponse.getDrtDecision()
                );

        RouteRecommendationResult result =
                buildResult(
                        candidateResult,
                        scoringResponse,
                        drtGuide
                );

        routeHistoryService.saveRecommendation(
                userId,
                request,
                result
        );

        return result;
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
                        .collect(
                                HashSet::new,
                                Set::add,
                                Set::addAll
                        );

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
            AiRouteScoringResponse scoringResponse,
            DrtGuideResponse drtGuide
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

        List<AiRouteScoringResponse.Result> scoredResults =
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
                        .toList();

        AiRouteScoringResponse.Result topResult =
                scoredResults.isEmpty()
                        ? null
                        : scoredResults.get(0);

        AiRouteScoringResponse.Result secondResult =
                scoredResults.size() < 2
                        ? null
                        : scoredResults.get(1);

        RouteCandidate topCandidate =
                topResult == null
                        ? null
                        : candidateMap.get(
                        topResult.getRouteId()
                );

        RouteCandidate secondCandidate =
                secondResult == null
                        ? null
                        : candidateMap.get(
                        secondResult.getRouteId()
                );

        String recommendationReason =
                topResult == null
                        ? null
                        : routeRecommendationReasonService
                        .createReason(
                                topCandidate,
                                topResult,
                                secondCandidate,
                                secondResult
                        );

        List<RouteRecommendationItem> recommendations =
                scoredResults.stream()
                        .map(result -> {
                            RouteCandidate candidate =
                                    candidateMap.get(
                                            result.getRouteId()
                                    );

                            return RouteRecommendationItem.builder()
                                    .routeId(
                                            result.getRouteId()
                                    )
                                    .candidate(candidate)
                                    .score(
                                            result.getScore()
                                    )
                                    .rank(
                                            result.getRank()
                                    )
                                    .recommendationReason(
                                            result.getRank() == 1
                                                    ? recommendationReason
                                                    : null
                                    )
                                    .accessibilitySummary(
                                            routeAccessibilitySummaryMapper
                                                    .toSummary(
                                                            candidate,
                                                            candidateResult
                                                                    .getWalkingRoute(),
                                                            candidateResult
                                                                    .getTransitRoutes()
                                                    )
                                    )
                                    .scoreBreakdown(
                                            result.getScoreBreakdown()
                                    )
                                    .slopeSummary(
                                            result.getSlopeSummary()
                                    )
                                    .build();
                        })
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
                .drtGuide(
                        drtGuide
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