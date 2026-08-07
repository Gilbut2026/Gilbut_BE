package com.gilbeot.gilbut.domain.station;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransitStation {

    private String stationId;
    private String name;
    private String normalizedName;
    private String address;
    private double latitude;
    private double longitude;
    private int distanceMeters;
}
