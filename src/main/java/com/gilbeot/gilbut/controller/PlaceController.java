package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.dto.place.request.PlaceSearchRequest;
import com.gilbeot.gilbut.dto.place.response.PlaceSearchResponse;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.place.PlaceSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceSearchService placeSearchService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PlaceSearchResponse>> searchPlaces(
            @ModelAttribute PlaceSearchRequest request
    ) {
        PlaceSearchResponse response =
                placeSearchService.search(request);

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }
}
