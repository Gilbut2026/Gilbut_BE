package com.gilbeot.gilbut.dto.station.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StationElevatorItemResponse {

    private String stationId;
    private String stationName;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer distanceMeters;
    private Integer elevatorCount;
    private List<StationElevatorDetailResponse> elevators;
}
