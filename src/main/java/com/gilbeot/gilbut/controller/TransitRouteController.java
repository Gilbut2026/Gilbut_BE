package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.dto.route.transit.request.TransitRouteRequest;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteResponse;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.route.TransitRouteService;
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
public class TransitRouteController {

    private final TransitRouteService transitRouteService;

    @PostMapping("/transit")
    public ResponseEntity<ApiResponse<TransitRouteResponse>>
    searchTransitRoute(
            @Valid @RequestBody TransitRouteRequest request
    ) {
        TransitRouteResponse response =
                transitRouteService.search(request);

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }
}
