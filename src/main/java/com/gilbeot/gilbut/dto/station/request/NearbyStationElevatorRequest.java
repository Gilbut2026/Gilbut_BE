package com.gilbeot.gilbut.dto.station.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NearbyStationElevatorRequest {

    private String lat;
    private String lng;
    private String radiusMeters;
}
