package com.gilbeot.gilbut.dto.route.transit.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransitStopResponse {

    private Integer stopIndex;
    private String stationId;
    private String name;
    private Double latitude;
    private Double longitude;
}
