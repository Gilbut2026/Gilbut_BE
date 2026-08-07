package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.dto.station.request.AlongRouteStationElevatorRequest;
import com.gilbeot.gilbut.dto.station.request.NearbyStationElevatorRequest;
import com.gilbeot.gilbut.dto.station.response.AlongRouteStationElevatorResponse;
import com.gilbeot.gilbut.dto.station.response.NearbyStationElevatorResponse;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.station.StationElevatorAlongRouteService;
import com.gilbeot.gilbut.service.station.StationElevatorNearbyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stations/elevators")
public class StationElevatorController {

    private final StationElevatorNearbyService
            stationElevatorNearbyService;
    private final StationElevatorAlongRouteService
            stationElevatorAlongRouteService;

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

    @PostMapping("/along-route")
    public ResponseEntity<ApiResponse<AlongRouteStationElevatorResponse>>
    getAlongRouteStationElevators(
            @Valid @RequestBody
            AlongRouteStationElevatorRequest request
    ) {
        AlongRouteStationElevatorResponse response =
                stationElevatorAlongRouteService.findAlongRoute(
                        request
                );

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }
}
