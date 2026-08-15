package com.gilbeot.gilbut.dto.route;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RouteAccessibilitySummary {

    private RouteAccessibilitySignals.Signal stair;
    private RouteAccessibilitySignals.Signal overpass;
    private RouteAccessibilitySignals.Signal underpass;
    private RouteAccessibilitySignals.Signal crosswalk;
}