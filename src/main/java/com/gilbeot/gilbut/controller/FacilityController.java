package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.dto.facility.request.NearbyFacilityRequest;
import com.gilbeot.gilbut.dto.facility.response.NearbyFacilityResponse;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.facility.FacilityNearbyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/facilities")
public class FacilityController {

    private final FacilityNearbyService facilityNearbyService;

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
}
