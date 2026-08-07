package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.dto.facility.request.AlongRouteFacilityRequest;
import com.gilbeot.gilbut.dto.facility.request.NearbyFacilityRequest;
import com.gilbeot.gilbut.dto.facility.response.AlongRouteFacilityResponse;
import com.gilbeot.gilbut.dto.facility.response.NearbyFacilityResponse;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.facility.FacilityAlongRouteService;
import com.gilbeot.gilbut.service.facility.FacilityNearbyService;
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
@RequestMapping("/api/facilities")
public class FacilityController {

    private final FacilityNearbyService facilityNearbyService;
    private final FacilityAlongRouteService facilityAlongRouteService;

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<NearbyFacilityResponse>>
    getNearbyFacilities(
            @ModelAttribute NearbyFacilityRequest request
    ) {
        NearbyFacilityResponse response =
                facilityNearbyService.findNearby(request);

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    @PostMapping("/along-route")
    public ResponseEntity<ApiResponse<AlongRouteFacilityResponse>>
    getAlongRouteFacilities(
            @Valid @RequestBody AlongRouteFacilityRequest request
    ) {
        AlongRouteFacilityResponse response =
                facilityAlongRouteService.findAlongRoute(request);

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }
}
