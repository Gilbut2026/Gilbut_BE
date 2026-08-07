package com.gilbeot.gilbut.dto.station.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NearbyStationElevatorResponse {

    private List<StationElevatorItemResponse> stations;
}
