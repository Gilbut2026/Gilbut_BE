package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.dto.route.walking.request.NavigationRerouteRequest;
import com.gilbeot.gilbut.dto.route.walking.request.WalkingRouteRequest;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteResponse;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.route.WalkingRerouteService;
import com.gilbeot.gilbut.service.route.WalkingRouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routes")
public class RouteController {

    private final WalkingRouteService walkingRouteService;
    private final WalkingRerouteService walkingRerouteService;

    @PostMapping("/walking")
    public ResponseEntity<ApiResponse<WalkingRouteResponse>>
    searchWalkingRoute(
            @Valid @RequestBody WalkingRouteRequest request
    ) {
        WalkingRouteResponse response =
                walkingRouteService.search(request);

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    @PostMapping("/walking/reroute")
    public ResponseEntity<ApiResponse<WalkingRouteResponse>>
    rerouteWalkingRoute(
            @Valid @RequestBody NavigationRerouteRequest request
    ) {
        WalkingRouteResponse response =
                walkingRerouteService.reroute(request);

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }
}
