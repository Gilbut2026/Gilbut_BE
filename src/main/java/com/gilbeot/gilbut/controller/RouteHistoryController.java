package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.dto.route.history.response.RouteHistoryDetailResponse;
import com.gilbeot.gilbut.dto.route.history.response.RouteHistoryResponse;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.history.RouteHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routes/history")
public class RouteHistoryController {

    private final RouteHistoryService routeHistoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RouteHistoryResponse>>>
    getHistories(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        List<RouteHistoryResponse> response =
                routeHistoryService.getHistories(
                        userId
                );

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    @GetMapping("/{historyId}")
    public ResponseEntity<ApiResponse<RouteHistoryDetailResponse>>
    getHistory(
            Authentication authentication,
            @PathVariable Long historyId
    ) {
        Long userId = extractUserId(authentication);

        RouteHistoryDetailResponse response =
                routeHistoryService.getHistory(
                        userId,
                        historyId
                );

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    @DeleteMapping("/{historyId}")
    public ResponseEntity<ApiResponse<Void>>
    deleteHistory(
            Authentication authentication,
            @PathVariable Long historyId
    ) {
        Long userId = extractUserId(authentication);

        routeHistoryService.deleteHistory(
                userId,
                historyId
        );

        return ApiResponse.success(
                SuccessCode._OK,
                null
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
