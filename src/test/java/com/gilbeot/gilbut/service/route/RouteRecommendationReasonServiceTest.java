package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringResponse;
import com.gilbeot.gilbut.dto.route.RouteAccessibilitySignals;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteMetrics;
import com.gilbeot.gilbut.dto.route.RouteWalkSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RouteRecommendationReasonServiceTest {

    private RouteRecommendationReasonService service;

    @BeforeEach
    void setUp() {
        service =
                new RouteRecommendationReasonService();
    }

    @Test
    @DisplayName("걷는 시간은 더 길지만 장애물과 경사가 유리하면 장단점을 함께 설명한다")
    void createReasonWithTradeOff() {
        RouteCandidate topCandidate =
                createCandidate(
                        "route-1",
                        660,
                        900,
                        1,
                        RouteAccessibilitySignals.known(
                                0,
                                0,
                                0
                        )
                );

        RouteCandidate secondCandidate =
                createCandidate(
                        "route-2",
                        480,
                        900,
                        1,
                        RouteAccessibilitySignals.known(
                                2,
                                0,
                                0
                        )
                );

        AiRouteScoringResponse.Result topResult =
                createResult(
                        "route-1",
                        1,
                        2.0,
                        1.0,
                        0.0,
                        1.0,
                        0.0,
                        0.2,
                        3.0
                );

        AiRouteScoringResponse.Result secondResult =
                createResult(
                        "route-2",
                        2,
                        0.5,
                        1.0,
                        4.0,
                        1.0,
                        0.0,
                        2.0,
                        7.0
                );

        String reason =
                service.createReason(
                        topCandidate,
                        topResult,
                        secondCandidate,
                        secondResult
                );

        assertThat(reason)
                .isEqualTo(
                        "걷는 시간은 약 3분 더 걸리지만, "
                                + "계단 같은 이동 장애물이 적고 "
                                + "오르막 경사도 더 완만해 "
                                + "이 경로를 추천했어요."
                );
    }

    @Test
    @DisplayName("걷는 시간과 환승이 유리하면 두 조건을 자연스럽게 설명한다")
    void createReasonWithWalkTimeAndTransfer() {
        RouteCandidate topCandidate =
                createCandidate(
                        "route-1",
                        420,
                        700,
                        0,
                        null
                );

        RouteCandidate secondCandidate =
                createCandidate(
                        "route-2",
                        780,
                        700,
                        1,
                        null
                );

        AiRouteScoringResponse.Result topResult =
                createResult(
                        "route-1",
                        1,
                        0.5,
                        1.0,
                        1.0,
                        0.0,
                        0.0,
                        1.0,
                        null
                );

        AiRouteScoringResponse.Result secondResult =
                createResult(
                        "route-2",
                        2,
                        3.0,
                        1.0,
                        1.0,
                        2.0,
                        0.0,
                        1.0,
                        null
                );

        String reason =
                service.createReason(
                        topCandidate,
                        topResult,
                        secondCandidate,
                        secondResult
                );

        assertThat(reason)
                .isEqualTo(
                        "걷는 시간이 비교적 짧고 환승도 적어 "
                                + "이 경로를 추천했어요."
                );
    }

    @Test
    @DisplayName("걷는 시간과 걷는 거리가 모두 유리하면 함께 설명한다")
    void createReasonWithWalkTimeAndDistance() {
        RouteCandidate topCandidate =
                createCandidate(
                        "route-1",
                        420,
                        600,
                        1,
                        null
                );

        RouteCandidate secondCandidate =
                createCandidate(
                        "route-2",
                        600,
                        900,
                        1,
                        null
                );

        AiRouteScoringResponse.Result topResult =
                createResult(
                        "route-1",
                        1,
                        0.5,
                        0.3,
                        1.0,
                        1.0,
                        0.0,
                        1.0,
                        null
                );

        AiRouteScoringResponse.Result secondResult =
                createResult(
                        "route-2",
                        2,
                        2.5,
                        2.0,
                        1.0,
                        1.0,
                        0.0,
                        1.0,
                        null
                );

        String reason =
                service.createReason(
                        topCandidate,
                        topResult,
                        secondCandidate,
                        secondResult
                );

        assertThat(reason)
                .isEqualTo(
                        "걷는 시간과 걸어야 하는 거리가 모두 적어 "
                                + "이 경로를 추천했어요."
                );
    }

    @Test
    @DisplayName("장애물 정보가 미확인이면 장애물이 적다고 추정하지 않는다")
    void doesNotUseUnknownObstacleInformation() {
        RouteCandidate topCandidate =
                createCandidate(
                        "route-1",
                        600,
                        800,
                        1,
                        RouteAccessibilitySignals.unknown()
                );

        RouteCandidate secondCandidate =
                createCandidate(
                        "route-2",
                        600,
                        800,
                        1,
                        RouteAccessibilitySignals.known(
                                3,
                                0,
                                0
                        )
                );

        AiRouteScoringResponse.Result topResult =
                createResult(
                        "route-1",
                        1,
                        1.0,
                        1.0,
                        0.0,
                        1.0,
                        0.0,
                        1.0,
                        null
                );

        AiRouteScoringResponse.Result secondResult =
                createResult(
                        "route-2",
                        2,
                        1.0,
                        1.0,
                        5.0,
                        1.0,
                        0.0,
                        1.0,
                        null
                );

        String reason =
                service.createReason(
                        topCandidate,
                        topResult,
                        secondCandidate,
                        secondResult
                );

        assertThat(reason)
                .isEqualTo(
                        "저장된 이동 설정과 경로 조건을 종합했을 때 "
                                + "가장 이동하기 편한 경로로 판단했어요."
                );

        assertThat(reason)
                .doesNotContain("계단");
    }

    @Test
    @DisplayName("경사 분석이 성공하지 않았으면 경사가 완만하다고 추정하지 않는다")
    void doesNotUseFailedSlopeAnalysis() {
        RouteCandidate topCandidate =
                createCandidate(
                        "route-1",
                        600,
                        800,
                        1,
                        null
                );

        RouteCandidate secondCandidate =
                createCandidate(
                        "route-2",
                        600,
                        800,
                        1,
                        null
                );

        AiRouteScoringResponse.Result topResult =
                AiRouteScoringResponse.Result.builder()
                        .routeId("route-1")
                        .rank(1)
                        .scoreBreakdown(
                                AiRouteScoringResponse.ScoreBreakdown
                                        .builder()
                                        .walkTimePenalty(1.0)
                                        .walkDistancePenalty(1.0)
                                        .obstaclePenalty(1.0)
                                        .transferPenalty(1.0)
                                        .weatherPenalty(0.0)
                                        .slopePenalty(0.0)
                                        .build()
                        )
                        .slopeSummary(
                                AiRouteScoringResponse.SlopeSummary
                                        .builder()
                                        .status(
                                                AiRouteScoringResponse
                                                        .SlopeAnalysisStatus
                                                        .FAILED
                                        )
                                        .maxUphillGradePercent(2.0)
                                        .build()
                        )
                        .build();

        AiRouteScoringResponse.Result secondResult =
                AiRouteScoringResponse.Result.builder()
                        .routeId("route-2")
                        .rank(2)
                        .scoreBreakdown(
                                AiRouteScoringResponse.ScoreBreakdown
                                        .builder()
                                        .walkTimePenalty(1.0)
                                        .walkDistancePenalty(1.0)
                                        .obstaclePenalty(1.0)
                                        .transferPenalty(1.0)
                                        .weatherPenalty(0.0)
                                        .slopePenalty(4.0)
                                        .build()
                        )
                        .slopeSummary(
                                AiRouteScoringResponse.SlopeSummary
                                        .builder()
                                        .status(
                                                AiRouteScoringResponse
                                                        .SlopeAnalysisStatus
                                                        .SUCCESS
                                        )
                                        .maxUphillGradePercent(8.0)
                                        .build()
                        )
                        .build();

        String reason =
                service.createReason(
                        topCandidate,
                        topResult,
                        secondCandidate,
                        secondResult
                );

        assertThat(reason)
                .isEqualTo(
                        "저장된 이동 설정과 경로 조건을 종합했을 때 "
                                + "가장 이동하기 편한 경로로 판단했어요."
                );

        assertThat(reason)
                .doesNotContain("경사");
    }

    @Test
    @DisplayName("비교할 2순위 경로가 없으면 기본 추천 이유를 반환한다")
    void createDefaultReasonWithoutSecondRoute() {
        RouteCandidate topCandidate =
                createCandidate(
                        "route-1",
                        600,
                        800,
                        0,
                        null
                );

        AiRouteScoringResponse.Result topResult =
                AiRouteScoringResponse.Result.builder()
                        .routeId("route-1")
                        .rank(1)
                        .build();

        String reason =
                service.createReason(
                        topCandidate,
                        topResult,
                        null,
                        null
                );

        assertThat(reason)
                .isEqualTo(
                        "저장된 이동 설정과 경로 조건을 종합했을 때 "
                                + "가장 이동하기 편한 경로로 판단했어요."
                );
    }

    private RouteCandidate createCandidate(
            String routeId,
            int walkTimeSec,
            int walkDistanceM,
            int transferCount,
            RouteAccessibilitySignals signals
    ) {
        RouteCandidate.RouteCandidateBuilder builder =
                RouteCandidate.builder()
                        .routeId(routeId)
                        .metrics(
                                RouteMetrics.builder()
                                        .totalTimeSec(1200)
                                        .totalWalkTimeSec(walkTimeSec)
                                        .totalWalkDistanceM(walkDistanceM)
                                        .transferCount(transferCount)
                                        .build()
                        );

        if (signals != null) {
            RouteWalkSegment walkSegment =
                    RouteWalkSegment.builder()
                            .walkSegmentId(
                                    routeId + "-walk-1"
                            )
                            .accessibilitySignals(signals)
                            .build();

            builder.walkSegments(
                    List.of(walkSegment)
            );
        }

        return builder.build();
    }

    private AiRouteScoringResponse.Result createResult(
            String routeId,
            int rank,
            double walkTimePenalty,
            double walkDistancePenalty,
            double obstaclePenalty,
            double transferPenalty,
            double weatherPenalty,
            double slopePenalty,
            Double maxUphillGradePercent
    ) {
        AiRouteScoringResponse.Result.ResultBuilder builder =
                AiRouteScoringResponse.Result.builder()
                        .routeId(routeId)
                        .rank(rank)
                        .scoreBreakdown(
                                AiRouteScoringResponse.ScoreBreakdown
                                        .builder()
                                        .walkTimePenalty(
                                                walkTimePenalty
                                        )
                                        .walkDistancePenalty(
                                                walkDistancePenalty
                                        )
                                        .obstaclePenalty(
                                                obstaclePenalty
                                        )
                                        .transferPenalty(
                                                transferPenalty
                                        )
                                        .weatherPenalty(
                                                weatherPenalty
                                        )
                                        .slopePenalty(
                                                slopePenalty
                                        )
                                        .build()
                        );

        if (maxUphillGradePercent != null) {
            builder.slopeSummary(
                    AiRouteScoringResponse.SlopeSummary
                            .builder()
                            .status(
                                    AiRouteScoringResponse
                                            .SlopeAnalysisStatus
                                            .SUCCESS
                            )
                            .maxUphillGradePercent(
                                    maxUphillGradePercent
                            )
                            .build()
            );
        }

        return builder.build();
    }
}