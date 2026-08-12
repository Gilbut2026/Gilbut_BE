package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.client.ai.AiRouteScoringClient;
import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringRequest;
import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringResponse;
import com.gilbeot.gilbut.client.ai.dto.scoring.type.ScoringResultStatus;
import com.gilbeot.gilbut.client.ai.mapper.AiRouteScoringRequestMapper;
import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
import com.gilbeot.gilbut.dto.route.RouteCandidateResult;
import com.gilbeot.gilbut.dto.route.RouteMetrics;
import com.gilbeot.gilbut.dto.route.RouteRecommendationResult;
import com.gilbeot.gilbut.dto.user.response.MobilityProfileResponse;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.service.drt.DrtGuideService;
import com.gilbeot.gilbut.service.user.UserMobilityProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteRecommendationServiceTest {

    @Mock
    private RouteCandidateAggregationService
            routeCandidateAggregationService;

    @Mock
    private RouteAccessibilityEnrichmentService
            routeAccessibilityEnrichmentService;

    @Mock
    private UserMobilityProfileService
            userMobilityProfileService;

    @Mock
    private AiRouteScoringRequestMapper
            aiRouteScoringRequestMapper;

    @Mock
    private AiRouteScoringClient
            aiRouteScoringClient;

    @Mock
    private DrtGuideService
            drtGuideService;

    private RouteRecommendationService
            routeRecommendationService;

    @BeforeEach
    void setUp() {
        routeRecommendationService =
                new RouteRecommendationService(
                        routeCandidateAggregationService,
                        routeAccessibilityEnrichmentService,
                        userMobilityProfileService,
                        aiRouteScoringRequestMapper,
                        aiRouteScoringClient,
                        drtGuideService
                );
    }

    @Test
    @DisplayName("AI 스코어링 결과를 순위대로 경로 후보와 매칭한다")
    void recommend() {
        Long userId = 1L;

        RouteCandidateRequest request =
                RouteCandidateRequest.builder()
                        .originLatitude(37.2636)
                        .originLongitude(127.0286)
                        .destinationLatitude(37.2750)
                        .destinationLongitude(127.0300)
                        .build();

        RouteCandidate candidate =
                RouteCandidate.builder()
                        .routeId("walking-1")
                        .routeType(
                                RouteType.WALKING
                        )
                        .routeOption(
                                WalkingRouteOption.DEFAULT
                        )
                        .providerRank(1)
                        .metrics(
                                RouteMetrics.builder()
                                        .totalTimeSec(900)
                                        .totalWalkTimeSec(900)
                                        .totalWalkDistanceM(1000)
                                        .transferCount(0)
                                        .build()
                        )
                        .build();

        RouteCandidateResult candidateResult =
                RouteCandidateResult.builder()
                        .requestId("request-1")
                        .candidates(
                                List.of(candidate)
                        )
                        .build();

        MobilityProfileResponse mobilityProfile =
                MobilityProfileResponse
                        .builder()
                        .build();

        AiRouteScoringRequest scoringRequest =
                AiRouteScoringRequest.builder()
                        .requestId("request-1")
                        .candidates(
                                List.of()
                        )
                        .build();

        AiRouteScoringResponse.Result scoringResult =
                AiRouteScoringResponse
                        .Result
                        .builder()
                        .routeId("walking-1")
                        .status(
                                ScoringResultStatus.SCORED
                        )
                        .score(92.0)
                        .rank(1)
                        .filterCodes(
                                List.of()
                        )
                        .build();

        AiRouteScoringResponse scoringResponse =
                AiRouteScoringResponse
                        .builder()
                        .requestId("request-1")
                        .scoringVersion(
                                "accessibility-score-v1"
                        )
                        .results(
                                List.of(
                                        scoringResult
                                )
                        )
                        .build();

        when(
                routeCandidateAggregationService
                        .createCandidates(request)
        ).thenReturn(
                candidateResult
        );

        when(
                routeAccessibilityEnrichmentService
                        .enrich(candidateResult)
        ).thenReturn(
                candidateResult
        );

        when(
                userMobilityProfileService
                        .getMobilityProfile(userId)
        ).thenReturn(
                mobilityProfile
        );

        when(
                aiRouteScoringRequestMapper
                        .toRequest(
                                mobilityProfile,
                                candidateResult
                        )
        ).thenReturn(
                scoringRequest
        );

        when(
                aiRouteScoringClient.score(
                        scoringRequest
                )
        ).thenReturn(
                scoringResponse
        );

        RouteRecommendationResult result =
                routeRecommendationService
                        .recommend(
                                userId,
                                request
                        );

        assertThat(
                result.getRequestId()
        ).isEqualTo(
                "request-1"
        );

        assertThat(
                result.getRecommendations()
        ).hasSize(1);

        assertThat(
                result.getRecommendations()
                        .get(0)
                        .getRouteId()
        ).isEqualTo(
                "walking-1"
        );

        assertThat(
                result.getRecommendations()
                        .get(0)
                        .getRank()
        ).isEqualTo(1);

        assertThat(
                result.getRecommendations()
                        .get(0)
                        .getScore()
        ).isEqualTo(
                92.0
        );

        assertThat(
                result.getRecommendations()
                        .get(0)
                        .getCandidate()
        ).isSameAs(
                candidate
        );
    }

    @Test
    @DisplayName("AI 응답 requestId가 다르면 예외가 발생한다")
    void rejectsDifferentRequestId() {
        Long userId = 1L;

        RouteCandidateRequest request =
                RouteCandidateRequest
                        .builder()
                        .build();

        RouteCandidateResult candidateResult =
                RouteCandidateResult.builder()
                        .requestId("request-1")
                        .candidates(
                                List.of()
                        )
                        .build();

        MobilityProfileResponse mobilityProfile =
                MobilityProfileResponse
                        .builder()
                        .build();

        AiRouteScoringRequest scoringRequest =
                AiRouteScoringRequest
                        .builder()
                        .requestId("request-1")
                        .candidates(
                                List.of()
                        )
                        .build();

        AiRouteScoringResponse scoringResponse =
                AiRouteScoringResponse
                        .builder()
                        .requestId(
                                "different-request"
                        )
                        .results(
                                List.of()
                        )
                        .build();

        when(
                routeCandidateAggregationService
                        .createCandidates(request)
        ).thenReturn(
                candidateResult
        );

        when(
                routeAccessibilityEnrichmentService
                        .enrich(candidateResult)
        ).thenReturn(
                candidateResult
        );

        when(
                userMobilityProfileService
                        .getMobilityProfile(userId)
        ).thenReturn(
                mobilityProfile
        );

        when(
                aiRouteScoringRequestMapper
                        .toRequest(
                                mobilityProfile,
                                candidateResult
                        )
        ).thenReturn(
                scoringRequest
        );

        when(
                aiRouteScoringClient.score(
                        scoringRequest
                )
        ).thenReturn(
                scoringResponse
        );

        assertThatThrownBy(
                () ->
                        routeRecommendationService
                                .recommend(
                                        userId,
                                        request
                                )
        ).isInstanceOf(
                CustomException.class
        );
    }
}