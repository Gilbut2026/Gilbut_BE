package com.gilbeot.gilbut.dto.route.transit.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TransitWalkingStepResponse {

    private Integer stepIndex;
    private String instruction;
    private String streetName;
    private Integer distanceM;
    private List<TransitRoutePointResponse> points;
}
