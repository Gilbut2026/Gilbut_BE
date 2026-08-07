package com.gilbeot.gilbut.dto.route;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RouteCandidate {

    private String routeId;
    private Integer providerRank;
    private RouteMetrics metrics;
}