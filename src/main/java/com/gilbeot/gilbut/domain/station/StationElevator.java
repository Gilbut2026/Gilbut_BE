package com.gilbeot.gilbut.domain.station;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StationElevator {

    private String stationName;
    private String normalizedStationName;
    private String operator;
    private String routeName;
    private String exitNumber;
    private String location;
    private String floorRange;
    private String state;
    private String elevatorNumber;
    private String capacityCount;
    private String capacityWeight;
}
