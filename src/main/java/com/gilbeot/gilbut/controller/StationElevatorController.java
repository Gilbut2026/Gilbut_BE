package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.dto.station.request.NearbyStationElevatorRequest;
import com.gilbeot.gilbut.dto.station.response.NearbyStationElevatorResponse;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.station.StationElevatorNearbyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stations/elevators")
public class StationElevatorController {

    private final StationElevatorNearbyService
            stationElevatorNearbyService;

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<NearbyStationElevatorResponse>>
    getNearbyStationElevators(
            @ModelAttribute NearbyStationElevatorRequest request
    ) {
        NearbyStationElevatorResponse response =
                stationElevatorNearbyService.findNearby(request);

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }
}
