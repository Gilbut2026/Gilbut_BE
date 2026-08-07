package com.gilbeot.gilbut.dto.route.transit.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TransitLegResponse {

    private Integer legIndex;
    private String mode;
    private String routeName;
    private String routeColor;
    private String providerRouteId;
    private Integer vehicleType;
    private Boolean serviceAvailable;
    private String startName;
    private Double startLatitude;
    private Double startLongitude;
    private String endName;
    private Double endLatitude;
    private Double endLongitude;
    private Integer distanceM;
    private Integer durationSec;
    private Integer stationCount;
    private List<TransitRoutePointResponse> routePoints;
    private List<TransitStopResponse> stops;
    private List<TransitWalkingStepResponse> steps;
}
