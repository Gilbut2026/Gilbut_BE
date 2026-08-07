package com.gilbeot.gilbut.dto.route.walking.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoutePointResponse {

    private Double latitude;
    private Double longitude;
}
