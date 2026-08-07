package com.gilbeot.gilbut.dto.route.transit.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransitRoutePointResponse {

    private Double latitude;
    private Double longitude;
}
