package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.dto.route.RouteRecommendationResult;
import com.gilbeot.gilbut.dto.route.request.RouteRecommendationRequest;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.route.RouteRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routes")
public class RouteRecommendationController {

    private final RouteRecommendationService routeRecommendationService;

    @PostMapping("/recommendations")
    public ResponseEntity<ApiResponse<RouteRecommendationResult>>
    recommend(
            Authentication authentication,
            @Valid @RequestBody RouteRecommendationRequest request
    ) {
        Long userId = extractUserId(authentication);

        RouteRecommendationResult response =
                routeRecommendationService.recommend(
                        userId,
                        request.toCandidateRequest()
                );

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    private Long extractUserId(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {

            throw new IllegalStateException(
                    "인증된 사용자 정보를 확인할 수 없습니다."
            );
        }

        try {
            return Long.parseLong(
                    authentication.getName()
            );

        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "인증된 사용자 ID 형식이 올바르지 않습니다.",
                    e
            );
        }
    }
}