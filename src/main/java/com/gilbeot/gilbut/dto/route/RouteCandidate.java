package com.gilbeot.gilbut.dto.route;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RouteCandidate {

    private String routeId;
    private RouteType routeType;
    private WalkingRouteOption routeOption;
    private Integer providerRank;
    private RouteMetrics metrics;

    /** AI 고도 보강에만 쓰며 공개 추천 응답에는 원본 좌표를 노출하지 않는다. */
    @JsonIgnore
    private List<RouteWalkSegment> walkSegments;
}
