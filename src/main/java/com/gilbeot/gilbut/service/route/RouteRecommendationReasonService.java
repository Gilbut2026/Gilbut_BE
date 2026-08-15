package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringResponse;
import com.gilbeot.gilbut.dto.route.RouteAccessibilitySignals;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteMetrics;
import com.gilbeot.gilbut.dto.route.RouteWalkSegment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RouteRecommendationReasonService {

    private static final int WALK_TIME_DIFFERENCE_SEC = 60;
    private static final int WALK_DISTANCE_DIFFERENCE_M = 100;
    private static final int WALK_TIME_TRADE_OFF_SEC = 180;
    private static final int WALK_DISTANCE_TRADE_OFF_M = 300;
    private static final double SLOPE_DIFFERENCE_PERCENT = 1.0;
    private static final double PENALTY_DIFFERENCE = 0.01;

    public String createReason(
            RouteCandidate topCandidate,
            AiRouteScoringResponse.Result topResult,
            RouteCandidate secondCandidate,
            AiRouteScoringResponse.Result secondResult
    ) {
        if (topCandidate == null || topResult == null) {
            return defaultReason();
        }

        if (secondCandidate == null || secondResult == null) {
            return defaultReason();
        }

        List<ReasonFactor> factors =
                findAdvantages(
                        topCandidate,
                        topResult,
                        secondCandidate,
                        secondResult
                );

        if (factors.isEmpty()) {
            return defaultReason();
        }

        factors.sort(
                Comparator.comparingDouble(
                        ReasonFactor::importance
                ).reversed()
        );

        List<ReasonType> mainReasons =
                factors.stream()
                        .limit(2)
                        .map(ReasonFactor::type)
                        .toList();

        String tradeOff =
                createTradeOff(
                        topCandidate,
                        secondCandidate
                );

        return buildMessage(
                tradeOff,
                mainReasons
        );
    }

    private List<ReasonFactor> findAdvantages(
            RouteCandidate topCandidate,
            AiRouteScoringResponse.Result topResult,
            RouteCandidate secondCandidate,
            AiRouteScoringResponse.Result secondResult
    ) {
        List<ReasonFactor> factors =
                new ArrayList<>();

        RouteMetrics topMetrics =
                topCandidate.getMetrics();

        RouteMetrics secondMetrics =
                secondCandidate.getMetrics();

        AiRouteScoringResponse.ScoreBreakdown topBreakdown =
                topResult.getScoreBreakdown();

        AiRouteScoringResponse.ScoreBreakdown secondBreakdown =
                secondResult.getScoreBreakdown();

        if (topMetrics == null
                || secondMetrics == null
                || topBreakdown == null
                || secondBreakdown == null) {
            return factors;
        }

        if (isMeaningfullyLower(
                topMetrics.getTotalWalkTimeSec(),
                secondMetrics.getTotalWalkTimeSec(),
                WALK_TIME_DIFFERENCE_SEC
        )) {
            addFactor(
                    factors,
                    ReasonType.WALK_TIME,
                    topBreakdown.getWalkTimePenalty(),
                    secondBreakdown.getWalkTimePenalty()
            );
        }

        if (isMeaningfullyLower(
                topMetrics.getTotalWalkDistanceM(),
                secondMetrics.getTotalWalkDistanceM(),
                WALK_DISTANCE_DIFFERENCE_M
        )) {
            addFactor(
                    factors,
                    ReasonType.WALK_DISTANCE,
                    topBreakdown.getWalkDistancePenalty(),
                    secondBreakdown.getWalkDistancePenalty()
            );
        }

        if (isLower(
                topMetrics.getTransferCount(),
                secondMetrics.getTransferCount()
        )) {
            addFactor(
                    factors,
                    ReasonType.TRANSFER,
                    topBreakdown.getTransferPenalty(),
                    secondBreakdown.getTransferPenalty()
            );
        }

        Integer topObstacleCount =
                getObstacleCount(topCandidate);

        Integer secondObstacleCount =
                getObstacleCount(secondCandidate);

        if (topObstacleCount != null
                && secondObstacleCount != null
                && topObstacleCount < secondObstacleCount) {
            addFactor(
                    factors,
                    ReasonType.OBSTACLE,
                    topBreakdown.getObstaclePenalty(),
                    secondBreakdown.getObstaclePenalty()
            );
        }

        if (hasGentlerSlope(
                topResult.getSlopeSummary(),
                secondResult.getSlopeSummary()
        )) {
            addFactor(
                    factors,
                    ReasonType.SLOPE,
                    topBreakdown.getSlopePenalty(),
                    secondBreakdown.getSlopePenalty()
            );
        }

        return factors;
    }

    private void addFactor(
            List<ReasonFactor> factors,
            ReasonType type,
            Double topPenalty,
            Double secondPenalty
    ) {
        if (topPenalty == null
                || secondPenalty == null) {
            return;
        }

        double difference =
                secondPenalty - topPenalty;

        if (difference > PENALTY_DIFFERENCE) {
            factors.add(
                    new ReasonFactor(
                            type,
                            difference
                    )
            );
        }
    }

    private String createTradeOff(
            RouteCandidate topCandidate,
            RouteCandidate secondCandidate
    ) {
        RouteMetrics top =
                topCandidate.getMetrics();

        RouteMetrics second =
                secondCandidate.getMetrics();

        if (top == null || second == null) {
            return null;
        }

        if (isMeaningfullyHigher(
                top.getTotalWalkTimeSec(),
                second.getTotalWalkTimeSec(),
                WALK_TIME_TRADE_OFF_SEC
        )) {
            int differenceMinutes =
                    Math.max(
                            1,
                            (top.getTotalWalkTimeSec()
                                    - second.getTotalWalkTimeSec()
                                    + 59) / 60
                    );

            return "걷는 시간은 약 "
                    + differenceMinutes
                    + "분 더 걸리지만";
        }

        if (isMeaningfullyHigher(
                top.getTotalWalkDistanceM(),
                second.getTotalWalkDistanceM(),
                WALK_DISTANCE_TRADE_OFF_M
        )) {
            int difference =
                    top.getTotalWalkDistanceM()
                            - second.getTotalWalkDistanceM();

            return "걷는 거리는 약 "
                    + difference
                    + "m 더 길지만";
        }

        return null;
    }

    private String buildMessage(
            String tradeOff,
            List<ReasonType> reasons
    ) {
        String reasonText =
                createNaturalReason(reasons);

        if (tradeOff != null) {
            return tradeOff
                    + ", "
                    + reasonText
                    + " 이 경로를 추천했어요.";
        }

        return reasonText
                + " 이 경로를 추천했어요.";
    }

    private String createNaturalReason(
            List<ReasonType> reasons
    ) {
        if (reasons.isEmpty()) {
            return "이동 부담이 가장 적어";
        }

        ReasonType first =
                reasons.get(0);

        if (reasons.size() == 1) {
            return singleReason(first);
        }

        ReasonType second =
                reasons.get(1);

        if (contains(
                first,
                second,
                ReasonType.OBSTACLE,
                ReasonType.SLOPE
        )) {
            return "계단 같은 이동 장애물이 적고 오르막 경사도 더 완만해";
        }

        if (contains(
                first,
                second,
                ReasonType.WALK_TIME,
                ReasonType.TRANSFER
        )) {
            return "걷는 시간이 비교적 짧고 환승도 적어";
        }

        if (contains(
                first,
                second,
                ReasonType.WALK_DISTANCE,
                ReasonType.TRANSFER
        )) {
            return "걸어야 하는 거리가 짧고 환승도 적어";
        }

        if (contains(
                first,
                second,
                ReasonType.WALK_TIME,
                ReasonType.OBSTACLE
        )) {
            return "걷는 시간이 비교적 짧고 이동 장애물도 적어";
        }

        if (contains(
                first,
                second,
                ReasonType.WALK_DISTANCE,
                ReasonType.OBSTACLE
        )) {
            return "걸어야 하는 거리가 짧고 이동 장애물도 적어";
        }

        if (contains(
                first,
                second,
                ReasonType.WALK_TIME,
                ReasonType.SLOPE
        )) {
            return "걷는 시간이 비교적 짧고 오르막 경사도 완만해";
        }

        if (contains(
                first,
                second,
                ReasonType.WALK_DISTANCE,
                ReasonType.SLOPE
        )) {
            return "걸어야 하는 거리가 짧고 오르막 경사도 완만해";
        }

        if (contains(
                first,
                second,
                ReasonType.TRANSFER,
                ReasonType.OBSTACLE
        )) {
            return "환승이 적고 계단 같은 이동 장애물도 적어";
        }

        if (contains(
                first,
                second,
                ReasonType.TRANSFER,
                ReasonType.SLOPE
        )) {
            return "환승이 적고 오르막 경사도 완만해";
        }

        if (contains(
                first,
                second,
                ReasonType.WALK_TIME,
                ReasonType.WALK_DISTANCE
        )) {
            return "걷는 시간과 걸어야 하는 거리가 모두 적어";
        }

        return singleReason(first);
    }

    private String singleReason(
            ReasonType type
    ) {
        return switch (type) {
            case OBSTACLE ->
                    "계단·육교·지하보도 같은 이동 장애물이 적어";
            case WALK_TIME ->
                    "걷는 시간이 비교적 짧아";
            case WALK_DISTANCE ->
                    "걸어야 하는 거리가 비교적 짧아";
            case TRANSFER ->
                    "환승 횟수가 적어";
            case SLOPE ->
                    "오르막 경사가 더 완만해";
        };
    }

    private boolean contains(
            ReasonType first,
            ReasonType second,
            ReasonType target1,
            ReasonType target2
    ) {
        return (first == target1
                && second == target2)
                || (first == target2
                && second == target1);
    }

    private Integer getObstacleCount(
            RouteCandidate candidate
    ) {
        if (candidate.getWalkSegments() == null
                || candidate.getWalkSegments().isEmpty()) {
            return null;
        }

        int total = 0;

        for (RouteWalkSegment segment
                : candidate.getWalkSegments()) {
            RouteAccessibilitySignals signals =
                    segment.getAccessibilitySignals();

            if (signals == null) {
                return null;
            }

            Integer stair =
                    getKnownCount(signals.getStair());

            Integer overpass =
                    getKnownCount(signals.getOverpass());

            Integer underpass =
                    getKnownCount(signals.getUnderpass());

            if (stair == null
                    || overpass == null
                    || underpass == null) {
                return null;
            }

            total += stair
                    + overpass
                    + underpass;
        }

        return total;
    }

    private Integer getKnownCount(
            RouteAccessibilitySignals.Signal signal
    ) {
        if (signal == null
                || signal.getState()
                == RouteAccessibilitySignals.State.UNKNOWN
                || signal.getCount() == null) {
            return null;
        }

        return signal.getCount();
    }

    private boolean hasGentlerSlope(
            AiRouteScoringResponse.SlopeSummary top,
            AiRouteScoringResponse.SlopeSummary second
    ) {
        if (!isReliableSlope(top)
                || !isReliableSlope(second)) {
            return false;
        }

        return top.getMaxUphillGradePercent()
                + SLOPE_DIFFERENCE_PERCENT
                < second.getMaxUphillGradePercent();
    }

    private boolean isReliableSlope(
            AiRouteScoringResponse.SlopeSummary slope
    ) {
        return slope != null
                && slope.getStatus()
                == AiRouteScoringResponse
                .SlopeAnalysisStatus.SUCCESS
                && slope.getMaxUphillGradePercent()
                != null;
    }

    private boolean isMeaningfullyLower(
            Integer first,
            Integer second,
            int threshold
    ) {
        return first != null
                && second != null
                && first + threshold <= second;
    }

    private boolean isMeaningfullyHigher(
            Integer first,
            Integer second,
            int threshold
    ) {
        return first != null
                && second != null
                && first >= second + threshold;
    }

    private boolean isLower(
            Integer first,
            Integer second
    ) {
        return first != null
                && second != null
                && first < second;
    }

    private String defaultReason() {
        return "저장된 이동 설정과 경로 조건을 종합했을 때 가장 이동하기 편한 경로로 판단했어요.";
    }

    private enum ReasonType {
        OBSTACLE,
        WALK_TIME,
        WALK_DISTANCE,
        TRANSFER,
        SLOPE
    }

    private record ReasonFactor(
            ReasonType type,
            double importance
    ) {
    }
}