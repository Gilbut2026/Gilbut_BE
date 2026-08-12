package com.gilbeot.gilbut.client.ai.dto.scoring;

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
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AiRouteScoringRequest {

    private String requestId;
    private UserContext userContext;
    private List<Candidate> candidates;

    @Getter
    @Builder
    public static class UserContext {

        private WalkingDuration walkingDuration;
        private StairLevel stairLevel;
        private SlopeLevel slopeLevel;
        private RestStopPreference restStopPreference;
        private TransferLevel transferLevel;
        private MobilityAid mobilityAid;
    }

    @Getter
    @Builder
    public static class Candidate {

        private String routeId;
        private RouteType routeType;
        private WalkingRouteOption routeOption;
        private Integer providerRank;
        private Metrics metrics;
        private List<WalkSegment> walkSegments;
    }

    @Getter
    @Builder
    public static class Metrics {

        private Integer totalTimeSec;
        private Integer totalWalkTimeSec;
        private Integer totalWalkDistanceM;
        private Integer transferCount;
    }

    @Getter
    @Builder
    public static class WalkSegment {

        private String walkSegmentId;
        private String role;
        private SegmentScope segmentScope;
        private Integer distanceM;
        private Integer durationSec;
        private Geometry geometry;
        private AccessibilitySignals accessibilitySignals;
    }

    @Getter
    @Builder
    public static class Geometry {

        private String type;
        private List<List<Double>> coordinates;
    }

    @Getter
    @Builder
    public static class AccessibilitySignals {

        private Signal stair;
        private Signal overpass;
        private Signal underpass;
    }

    @Getter
    @Builder
    public static class Signal {

        private AccessibilitySignalState state;
        private Integer count;
    }
}
