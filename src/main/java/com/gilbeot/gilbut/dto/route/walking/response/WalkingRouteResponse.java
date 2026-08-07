package com.gilbeot.gilbut.dto.route.walking.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WalkingRouteResponse {

    private List<WalkingRouteItemResponse> routes;
}
