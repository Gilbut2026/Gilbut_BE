package com.gilbeot.gilbut.dto.route.transit.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TransitRouteResponse {

    private List<TransitRouteItemResponse> routes;
}
