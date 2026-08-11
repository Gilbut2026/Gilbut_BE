package com.gilbeot.gilbut.dto.route;

import com.gilbeot.gilbut.client.ai.dto.scoring.type.SegmentScope;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RouteWalkSegment {

    private String walkSegmentId;
    private Role role;
    private SegmentScope segmentScope;
    private Integer distanceM;
    private Integer durationSec;
    private Geometry geometry;

    public enum Role {
        WALKING_ROUTE,
        ORIGIN_TO_FIRST_STOP,
        TRANSFER_WALK,
        LAST_STOP_TO_DESTINATION
    }

    @Getter
    @Builder
    public static class Geometry {

        private String type;
        private List<List<Double>> coordinates;
    }
}
