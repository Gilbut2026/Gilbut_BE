package com.gilbeot.gilbut.client.ai.mapper;

import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringRequest;
import com.gilbeot.gilbut.client.ai.dto.scoring.type.AccessibilitySignalState;
import com.gilbeot.gilbut.client.ai.dto.scoring.type.SegmentScope;
import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import com.gilbeot.gilbut.domain.user.type.MobilityAid;
import com.gilbeot.gilbut.domain.user.type.RestStopPreference;
import com.gilbeot.gilbut.domain.user.type.SlopeLevel;
import com.gilbeot.gilbut.domain.user.type.StairLevel;
import com.gilbeot.gilbut.domain.user.type.TransferLevel;
import com.gilbeot.gilbut.domain.user.type.WalkingDuration;
import com.gilbeot.gilbut.dto.route.RouteAccessibilitySignals;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteCandidateResult;
import com.gilbeot.gilbut.dto.route.RouteMetrics;
import com.gilbeot.gilbut.dto.route.RouteWalkSegment;
import com.gilbeot.gilbut.dto.user.response.MobilityProfileResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AiRouteScoringRequestMapperTest {

    private final AiRouteScoringRequestMapper mapper =
            new AiRouteScoringRequestMapper();

    @Test
    @DisplayName("사용자 이동 특성과 접근성 정보를 포함한 경로 후보를 AI 스코어링 요청으로 변환한다")
    void toRequest() {

        MobilityProfileResponse profile =
                MobilityProfileResponse.builder()
                        .walkingDuration(
                                WalkingDuration.WITHIN_20_MINUTES
                        )
                        .stairLevel(
                                StairLevel.SLIGHTLY_DIFFICULT
                        )
                        .slopeLevel(
                                SlopeLevel.SLIGHTLY_DIFFICULT
                        )
                        .restStopPreference(
                                RestStopPreference.REQUIRED
                        )
                        .transferLevel(
                                TransferLevel.FEWER_PREFERRED
                        )
                        .mobilityAid(
                                MobilityAid.CANE_OR_WALKER
                        )
                        .build();

        RouteCandidate walkingCandidate =
                RouteCandidate.builder()
                        .routeId("walking-1")
                        .routeType(RouteType.WALKING)
                        .routeOption(
                                WalkingRouteOption.AVOID_STAIRS
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
                        .walkSegments(
                                List.of(
                                        RouteWalkSegment.builder()
                                                .walkSegmentId(
                                                        "walking-1:walk:0"
                                                )
                                                .role(
                                                        RouteWalkSegment.Role.WALKING_ROUTE
                                                )
                                                .segmentScope(
                                                        SegmentScope.EXTERNAL_WALK
                                                )
                                                .distanceM(1000)
                                                .durationSec(900)
                                                .accessibilitySignals(
                                                        RouteAccessibilitySignals.known(
                                                                1,
                                                                0,
                                                                1
                                                        )
                                                )
                                                .build()
                                )
                        )
                        .build();

        RouteCandidate transitCandidate =
                RouteCandidate.builder()
                        .routeId("transit-1")
                        .routeType(RouteType.TRANSIT)
                        .providerRank(1)
                        .metrics(
                                RouteMetrics.builder()
                                        .totalTimeSec(1200)
                                        .totalWalkTimeSec(300)
                                        .totalWalkDistanceM(250)
                                        .transferCount(1)
                                        .build()
                        )
                        .build();

        RouteCandidateResult result =
                RouteCandidateResult.builder()
                        .requestId("request-1")
                        .candidates(
                                List.of(
                                        walkingCandidate,
                                        transitCandidate
                                )
                        )
                        .build();

        AiRouteScoringRequest request =
                mapper.toRequest(
                        profile,
                        result
                );

        assertThat(request.getRequestId())
                .isEqualTo("request-1");

        assertThat(
                request.getUserContext()
                        .getWalkingDuration()
        ).isEqualTo(
                WalkingDuration.WITHIN_20_MINUTES
        );

        assertThat(
                request.getUserContext()
                        .getStairLevel()
        ).isEqualTo(
                StairLevel.SLIGHTLY_DIFFICULT
        );

        assertThat(
                request.getUserContext()
                        .getSlopeLevel()
        ).isEqualTo(
                SlopeLevel.SLIGHTLY_DIFFICULT
        );

        assertThat(
                request.getUserContext()
                        .getRestStopPreference()
        ).isEqualTo(
                RestStopPreference.REQUIRED
        );

        assertThat(
                request.getUserContext()
                        .getTransferLevel()
        ).isEqualTo(
                TransferLevel.FEWER_PREFERRED
        );

        assertThat(
                request.getUserContext()
                        .getMobilityAid()
        ).isEqualTo(
                MobilityAid.CANE_OR_WALKER
        );

        assertThat(request.getCandidates())
                .hasSize(2);

        AiRouteScoringRequest.Candidate walkingRequestCandidate =
                request.getCandidates()
                        .get(0);

        assertThat(
                walkingRequestCandidate.getRouteId()
        ).isEqualTo("walking-1");

        assertThat(
                walkingRequestCandidate.getRouteOption()
        ).isEqualTo(
                WalkingRouteOption.AVOID_STAIRS
        );

        assertThat(
                walkingRequestCandidate.getMetrics()
                        .getTotalWalkDistanceM()
        ).isEqualTo(1000);

        assertThat(
                walkingRequestCandidate.getWalkSegments()
        ).hasSize(1);

        AiRouteScoringRequest.WalkSegment walkSegment =
                walkingRequestCandidate
                        .getWalkSegments()
                        .get(0);

        assertThat(
                walkSegment.getWalkSegmentId()
        ).isEqualTo("walking-1:walk:0");

        assertThat(
                walkSegment.getRole()
        ).isEqualTo("WALKING_ROUTE");

        assertThat(
                walkSegment.getSegmentScope()
        ).isEqualTo(
                SegmentScope.EXTERNAL_WALK
        );

        assertThat(
                walkSegment.getAccessibilitySignals()
        ).isNotNull();

        assertThat(
                walkSegment.getAccessibilitySignals()
                        .getStair()
                        .getState()
        ).isEqualTo(
                AccessibilitySignalState.PRESENT
        );

        assertThat(
                walkSegment.getAccessibilitySignals()
                        .getStair()
                        .getCount()
        ).isEqualTo(1);

        assertThat(
                walkSegment.getAccessibilitySignals()
                        .getOverpass()
                        .getState()
        ).isEqualTo(
                AccessibilitySignalState.ABSENT
        );

        assertThat(
                walkSegment.getAccessibilitySignals()
                        .getOverpass()
                        .getCount()
        ).isZero();

        assertThat(
                walkSegment.getAccessibilitySignals()
                        .getUnderpass()
                        .getState()
        ).isEqualTo(
                AccessibilitySignalState.PRESENT
        );

        assertThat(
                walkSegment.getAccessibilitySignals()
                        .getUnderpass()
                        .getCount()
        ).isEqualTo(1);

        AiRouteScoringRequest.Candidate transitRequestCandidate =
                request.getCandidates()
                        .get(1);

        assertThat(
                transitRequestCandidate.getRouteType()
        ).isEqualTo(
                RouteType.TRANSIT
        );

        assertThat(
                transitRequestCandidate.getRouteOption()
        ).isNull();

        assertThat(
                transitRequestCandidate.getWalkSegments()
        ).isEmpty();
    }
}