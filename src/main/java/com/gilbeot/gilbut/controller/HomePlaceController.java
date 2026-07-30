package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.dto.place.request.HomePlaceSaveRequest;
import com.gilbeot.gilbut.dto.place.response.HomePlaceResponse;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.user.HomePlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/home")
public class HomePlaceController {

    private final HomePlaceService homePlaceService;

    // 집 주소 조회
    @GetMapping
    public ResponseEntity<ApiResponse<HomePlaceResponse>>
    getHome(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        HomePlaceResponse response =
                homePlaceService.getHome(userId);

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    // 집 주소 등록 및 수정
    @PutMapping
    public ResponseEntity<ApiResponse<HomePlaceResponse>>
    saveHome(
            Authentication authentication,
            @Valid @RequestBody HomePlaceSaveRequest request
    ) {
        Long userId = extractUserId(authentication);

        HomePlaceResponse response =
                homePlaceService.saveHome(
                        userId,
                        request
                );

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    // 집 주소 삭제
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>>
    deleteHome(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        homePlaceService.deleteHome(userId);

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
            return Long.parseLong(authentication.getName());

        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "인증된 사용자 ID 형식이 올바르지 않습니다.",
                    e
            );
        }
    }
}