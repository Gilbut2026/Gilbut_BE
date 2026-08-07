package com.gilbeot.gilbut.dto.route.walking.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WalkingStepResponse {

    private Integer stepIndex;
    private String instruction;
    private Integer distanceM;
    private Integer durationSec;
    private Integer turnType;
    private String pointType;
    private List<RoutePointResponse> points;
}
