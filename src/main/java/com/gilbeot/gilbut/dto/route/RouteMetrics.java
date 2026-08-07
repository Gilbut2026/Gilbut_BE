package com.gilbeot.gilbut.dto.route;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RouteMetrics {

    private Integer totalTimeSec;
    private Integer totalWalkTimeSec;
    private Integer totalWalkDistanceM;
    private Integer transferCount;
}