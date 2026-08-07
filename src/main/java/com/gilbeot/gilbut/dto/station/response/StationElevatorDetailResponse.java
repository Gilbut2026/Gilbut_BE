package com.gilbeot.gilbut.dto.station.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StationElevatorDetailResponse {

    private String routeName;
    private String operator;
    private String exitNumber;
    private String location;
    private String floorRange;
    private String state;
    private String elevatorNumber;
    private String capacityCount;
    private String capacityWeight;
}
