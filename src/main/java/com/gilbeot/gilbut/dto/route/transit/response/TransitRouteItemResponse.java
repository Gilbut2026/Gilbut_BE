package com.gilbeot.gilbut.dto.route.transit.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TransitRouteItemResponse {

    private String routeId;
    private Integer providerRank;
    private TransitRouteSummaryResponse summary;
    private List<TransitRoutePointResponse> routePoints;
    private List<TransitLegResponse> legs;
}
