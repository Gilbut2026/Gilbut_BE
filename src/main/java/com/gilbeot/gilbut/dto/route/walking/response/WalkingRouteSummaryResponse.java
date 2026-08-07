package com.gilbeot.gilbut.dto.route.walking.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalkingRouteSummaryResponse {

    private Integer totalDistanceM;
    private Integer totalTimeSec;
}
