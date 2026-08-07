package com.gilbeot.gilbut.dto.route.transit.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransitRouteSummaryResponse {

    private Integer totalTimeSec;
    private Integer totalWalkTimeSec;
    private Integer totalWalkDistanceM;
    private Integer totalDistanceM;
    private Integer transferCount;
    private Integer fareKrw;
    private Integer pathType;
}
