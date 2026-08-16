package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.client.ai.AiRouteScoringClient;
import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringRequest;
import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringResponse;
import com.gilbeot.gilbut.client.ai.dto.scoring.type.ScoringResultStatus;
import com.gilbeot.gilbut.client.ai.mapper.AiRouteScoringRequestMapper;
import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import com.gilbeot.gilbut.dto.drt.DrtAvailability;
import com.gilbeot.gilbut.dto.drt.DrtGuideResponse;
import com.gilbeot.gilbut.dto.drt.DrtServiceArea;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
import com.gilbeot.gilbut.dto.route.RouteCandidateResult;
import com.gilbeot.gilbut.dto.route.RouteMetrics;
import com.gilbeot.gilbut.dto.route.RouteRecommendationResult;
import com.gilbeot.gilbut.dto.user.response.MobilityProfileResponse;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.service.drt.DrtGuideService;
import com.gilbeot.gilbut.service.history.RouteHistoryService;
import com.gilbeot.gilbut.service.user.UserMobilityProfileService;
import com.gilbeot.gilbut.service.chat.ChatSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteRecommendationServiceTest {

    @Mock
    private RouteCandidateAggregationService routeCandidateAggregationService;

    @Mock
    private RouteAccessibilityEnrichmentService routeAccessibilityEnrichmentService;

    @Mock
    private UserMobilityProfileService userMobilityProfileService;

    @Mock
    private AiRouteScoringRequestMapper aiRouteScoringRequestMapper;

    @Mock
    private AiRouteScoringClient aiRouteScoringClient;

    @Mock
    private DrtGuideService drtGuideService;

    @Mock
    private RouteRecommendationReasonService routeRecommendationReasonService;

    @Mock
    private RouteHistoryService routeHistoryService;

    @Mock
    private RouteAccessibilitySummaryMapper routeAccessibilitySummaryMapper;

    @Mock
    private ChatSessionService chatSessionService;

    private RouteRecommendationService routeRecommendationService;

    @BeforeEach
    void setUp() {
        routeRecommendationService =
                new RouteRecommendationService(
                        routeCandidateAggregationService,
                        routeAccessibilityEnrichmentService,
                        userMobilityProfileService,
                        aiRouteScoringRequestMapper,
                        aiRouteScoringClient,
                        drtGuideService,
                        routeAccessibilitySummaryMapper,
                        routeRecommendationReasonService,
                        routeHistoryService,
                        chatSessionService
                );
    }

    @Test
    @DisplayName("AI 스코어링 결과와 추천 이유 및 똑버스 안내를 경로 추천 결과로 반환한다")
    void recommend() {
        Long userId = 1L;

        LocalDateTime departureDateTime =
                LocalDateTime.of(
                        2026,
                        8,
                        16,
                        15,
                        0
                );

        RouteCandidateRequest request =
                RouteCandidateRequest.builder()
                        .originLatitude(37.2636)
                        .originLongitude(127.0286)
                        .destinationLatitude(37.2750)
                        .destinationLongitude(127.0300)
                        .departureDateTime(departureDateTime)
                        .build();

        RouteCandidate candidate =
                RouteCandidate.builder()
                        .routeId("walking-1")
                        .routeType(RouteType.WALKING)
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
                MobilityProfileResponse.builder()
                        .build();

        AiRouteScoringRequest scoringRequest =
                AiRouteScoringRequest.builder()
                        .requestId("request-1")
                        .departureDateTime(departureDateTime)
                        .candidates(List.of())
                        .build();

        AiRouteScoringResponse.Result scoringResult =
                AiRouteScoringResponse.Result.builder()
                        .routeId("walking-1")
                        .status(
                                ScoringResultStatus.SCORED
                        )
                        .score(92.0)
                        .rank(1)
                        .filterCodes(List.of())
                        .build();

        AiRouteScoringResponse.DrtDecision drtDecision =
                AiRouteScoringResponse.DrtDecision.builder()
                        .show(true)
                        .priority(true)
                        .taxiGuide(false)
                        .reasonCodes(List.of())
                        .basedOnRouteId("walking-1")
                        .build();

        AiRouteScoringResponse scoringResponse =
                AiRouteScoringResponse.builder()
                        .requestId("request-1")
                        .scoringVersion(
                                "accessibility-score-v1"
                        )
                        .results(
                                List.of(scoringResult)
                        )
                        .drtDecision(drtDecision)
                        .build();

        DrtGuideResponse drtGuide =
                DrtGuideResponse.builder()
                        .show(true)
                        .serviceName("수원 똑버스")
                        .serviceArea(
                                DrtServiceArea.GWANGGYO
                        )
                        .serviceAreaName("광교1·2동")
                        .availability(
                                DrtAvailability.CHECK_REQUIRED
                        )
                        .message(
                                "이 지역은 똑버스 운행 지역이에요. "
                                        + "실제 호출 가능한 정류장과 배차 여부는 "
                                        + "똑타 앱에서 확인해 주세요."
                        )
                        .build();

        String recommendationReason =
                "저장된 이동 설정과 경로 조건을 종합했을 때 "
                        + "가장 이동하기 편한 경로로 판단했어요.";

        when(
                routeCandidateAggregationService
                        .createCandidates(request)
        ).thenReturn(candidateResult);

        when(
                routeAccessibilityEnrichmentService
                        .enrich(candidateResult)
        ).thenReturn(candidateResult);

        when(
                userMobilityProfileService
                        .getMobilityProfile(userId)
        ).thenReturn(mobilityProfile);

        when(
                aiRouteScoringRequestMapper.toRequest(
                        mobilityProfile,
                        candidateResult,
                        departureDateTime
                )
        ).thenReturn(scoringRequest);

        when(
                aiRouteScoringClient.score(
                        scoringRequest
                )
        ).thenReturn(scoringResponse);

        when(
                drtGuideService.createGuide(
                        request,
                        drtDecision
                )
        ).thenReturn(drtGuide);

        when(
                routeRecommendationReasonService
                        .createReason(
                                candidate,
                                scoringResult,
                                null,
                                null
                        )
        ).thenReturn(
                recommendationReason
        );

        RouteRecommendationResult result =
                routeRecommendationService.recommend(
                        userId,
                        request
                );

        assertThat(result.getRequestId())
                .isEqualTo("request-1");

        assertThat(result.getRecommendations())
                .hasSize(1);

        assertThat(
                result.getRecommendations()
                        .get(0)
                        .getRouteId()
        ).isEqualTo("walking-1");

        assertThat(
                result.getRecommendations()
                        .get(0)
                        .getRank()
        ).isEqualTo(1);

        assertThat(
                result.getRecommendations()
                        .get(0)
                        .getScore()
        ).isEqualTo(92.0);

        assertThat(
                result.getRecommendations()
                        .get(0)
                        .getCandidate()
        ).isSameAs(candidate);

        assertThat(
                result.getRecommendations()
                        .get(0)
                        .getRecommendationReason()
        ).isEqualTo(
                recommendationReason
        );

        assertThat(result.getDrtDecision())
                .isSameAs(drtDecision);

        assertThat(result.getDrtGuide())
                .isSameAs(drtGuide);

        assertThat(
                result.getDrtGuide()
                        .getServiceArea()
        ).isEqualTo(
                DrtServiceArea.GWANGGYO
        );

        assertThat(
                result.getDrtGuide()
                        .getAvailability()
        ).isEqualTo(
                DrtAvailability.CHECK_REQUIRED
        );

        verify(
                aiRouteScoringRequestMapper
        ).toRequest(
                mobilityProfile,
                candidateResult,
                departureDateTime
        );

        verify(
                routeRecommendationReasonService
        ).createReason(
                candidate,
                scoringResult,
                null,
                null
        );

        verify(
                drtGuideService
        ).createGuide(
                request,
                drtDecision
        );

        verify(
                routeHistoryService
        ).saveRecommendation(
                userId,
                request,
                result
        );

        verify(
                chatSessionService
        ).completeRouteCalculationIfActive(
                userId,
                "request-1"
        );
    }

    @Test
    @DisplayName("AI 순위에 따라 경로를 정렬하고 1순위 경로에만 추천 이유를 포함한다")
    void recommendWithRankedRoutes() {
        Long userId = 1L;

        LocalDateTime departureDateTime =
                LocalDateTime.of(
                        2026,
                        8,
                        16,
                        15,
                        0
                );

        RouteCandidateRequest request =
                RouteCandidateRequest.builder()
                        .originLatitude(37.2636)
                        .originLongitude(127.0286)
                        .destinationLatitude(37.2750)
                        .destinationLongitude(127.0300)
                        .departureDateTime(departureDateTime)
                        .build();

        RouteCandidate firstCandidate =
                RouteCandidate.builder()
                        .routeId("route-1")
                        .routeType(RouteType.WALKING)
                        .routeOption(
                                WalkingRouteOption.DEFAULT
                        )
                        .providerRank(2)
                        .metrics(
                                RouteMetrics.builder()
                                        .totalTimeSec(900)
                                        .totalWalkTimeSec(600)
                                        .totalWalkDistanceM(800)
                                        .transferCount(0)
                                        .build()
                        )
                        .build();

        RouteCandidate secondCandidate =
                RouteCandidate.builder()
                        .routeId("route-2")
                        .routeType(RouteType.WALKING)
                        .routeOption(
                                WalkingRouteOption.DEFAULT
                        )
                        .providerRank(1)
                        .metrics(
                                RouteMetrics.builder()
                                        .totalTimeSec(800)
                                        .totalWalkTimeSec(500)
                                        .totalWalkDistanceM(700)
                                        .transferCount(0)
                                        .build()
                        )
                        .build();

        RouteCandidateResult candidateResult =
                RouteCandidateResult.builder()
                        .requestId("request-1")
                        .candidates(
                                List.of(
                                        firstCandidate,
                                        secondCandidate
                                )
                        )
                        .build();

        MobilityProfileResponse mobilityProfile =
                MobilityProfileResponse.builder()
                        .build();

        AiRouteScoringRequest scoringRequest =
                AiRouteScoringRequest.builder()
                        .requestId("request-1")
                        .departureDateTime(departureDateTime)
                        .candidates(List.of())
                        .build();

        AiRouteScoringResponse.Result rankTwoResult =
                AiRouteScoringResponse.Result.builder()
                        .routeId("route-2")
                        .status(
                                ScoringResultStatus.SCORED
                        )
                        .score(80.0)
                        .rank(2)
                        .filterCodes(List.of())
                        .build();

        AiRouteScoringResponse.Result rankOneResult =
                AiRouteScoringResponse.Result.builder()
                        .routeId("route-1")
                        .status(
                                ScoringResultStatus.SCORED
                        )
                        .score(92.0)
                        .rank(1)
                        .filterCodes(List.of())
                        .build();

        AiRouteScoringResponse scoringResponse =
                AiRouteScoringResponse.builder()
                        .requestId("request-1")
                        .scoringVersion(
                                "accessibility-score-v1"
                        )
                        .results(
                                List.of(
                                        rankTwoResult,
                                        rankOneResult
                                )
                        )
                        .build();

        String recommendationReason =
                "걷는 시간이 비교적 짧고 환승도 적어 "
                        + "이 경로를 추천했어요.";

        when(
                routeCandidateAggregationService
                        .createCandidates(request)
        ).thenReturn(candidateResult);

        when(
                routeAccessibilityEnrichmentService
                        .enrich(candidateResult)
        ).thenReturn(candidateResult);

        when(
                userMobilityProfileService
                        .getMobilityProfile(userId)
        ).thenReturn(mobilityProfile);

        when(
                aiRouteScoringRequestMapper.toRequest(
                        mobilityProfile,
                        candidateResult,
                        departureDateTime
                )
        ).thenReturn(scoringRequest);

        when(
                aiRouteScoringClient.score(
                        scoringRequest
                )
        ).thenReturn(scoringResponse);

        when(
                drtGuideService.createGuide(
                        request,
                        null
                )
        ).thenReturn(null);

        when(
                routeRecommendationReasonService
                        .createReason(
                                firstCandidate,
                                rankOneResult,
                                secondCandidate,
                                rankTwoResult
                        )
        ).thenReturn(
                recommendationReason
        );

        RouteRecommendationResult result =
                routeRecommendationService.recommend(
                        userId,
                        request
                );

        assertThat(
                result.getRecommendations()
        ).hasSize(2);

        assertThat(
                result.getRecommendations()
                        .get(0)
                        .getRouteId()
        ).isEqualTo("route-1");

        assertThat(
                result.getRecommendations()
                        .get(0)
                        .getRank()
        ).isEqualTo(1);

        assertThat(
                result.getRecommendations()
                        .get(0)
                        .getRecommendationReason()
        ).isEqualTo(
                recommendationReason
        );

        assertThat(
                result.getRecommendations()
                        .get(1)
                        .getRouteId()
        ).isEqualTo("route-2");

        assertThat(
                result.getRecommendations()
                        .get(1)
                        .getRank()
        ).isEqualTo(2);

        assertThat(
                result.getRecommendations()
                        .get(1)
                        .getRecommendationReason()
        ).isNull();

        verify(
                routeRecommendationReasonService
        ).createReason(
                firstCandidate,
                rankOneResult,
                secondCandidate,
                rankTwoResult
        );

        verify(
                routeHistoryService
        ).saveRecommendation(
                userId,
                request,
                result
        );
    }

    @Test
    @DisplayName("AI 응답 requestId가 다르면 예외가 발생한다")
    void rejectsDifferentRequestId() {
        Long userId = 1L;

        LocalDateTime departureDateTime =
                LocalDateTime.of(
                        2026,
                        8,
                        16,
                        15,
                        0
                );

        RouteCandidateRequest request =
                RouteCandidateRequest.builder()
                        .departureDateTime(departureDateTime)
                        .build();

        RouteCandidateResult candidateResult =
                RouteCandidateResult.builder()
                        .requestId("request-1")
                        .candidates(List.of())
                        .build();

        MobilityProfileResponse mobilityProfile =
                MobilityProfileResponse.builder()
                        .build();

        AiRouteScoringRequest scoringRequest =
                AiRouteScoringRequest.builder()
                        .requestId("request-1")
                        .departureDateTime(departureDateTime)
                        .candidates(List.of())
                        .build();

        AiRouteScoringResponse scoringResponse =
                AiRouteScoringResponse.builder()
                        .requestId("different-request")
                        .results(List.of())
                        .build();

        when(
                routeCandidateAggregationService
                        .createCandidates(request)
        ).thenReturn(candidateResult);

        when(
                routeAccessibilityEnrichmentService
                        .enrich(candidateResult)
        ).thenReturn(candidateResult);

        when(
                userMobilityProfileService
                        .getMobilityProfile(userId)
        ).thenReturn(mobilityProfile);

        when(
                aiRouteScoringRequestMapper.toRequest(
                        mobilityProfile,
                        candidateResult,
                        departureDateTime
                )
        ).thenReturn(scoringRequest);

        when(
                aiRouteScoringClient.score(
                        scoringRequest
                )
        ).thenReturn(scoringResponse);

        assertThatThrownBy(
                () -> routeRecommendationService.recommend(
                        userId,
                        request
                )
        ).isInstanceOf(
                CustomException.class
        );
    }
}