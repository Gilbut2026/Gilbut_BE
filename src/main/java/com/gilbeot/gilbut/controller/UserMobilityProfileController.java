package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.dto.user.request.MobilityProfileSaveRequest;
import com.gilbeot.gilbut.dto.user.response.MobilityProfileResponse;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.user.UserMobilityProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/mobility-profile")
public class UserMobilityProfileController {

    private final UserMobilityProfileService mobilityProfileService;

    // 사용자의 이동 특성 조회

    @GetMapping
    public ResponseEntity<ApiResponse<MobilityProfileResponse>>
    getMobilityProfile(Authentication authentication) {

        Long userId = extractUserId(authentication);

        MobilityProfileResponse response =
                mobilityProfileService.getMobilityProfile(userId);

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    // 사용자의 이동 특성 신규 저장 또는 수정

    @PutMapping
    public ResponseEntity<ApiResponse<MobilityProfileResponse>>
    saveMobilityProfile(
            Authentication authentication,
            @Valid @RequestBody MobilityProfileSaveRequest request
    ) {
        Long userId = extractUserId(authentication);

        MobilityProfileResponse response =
                mobilityProfileService.saveMobilityProfile(
                        userId,
                        request
                );

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    private Long extractUserId(Authentication authentication) {
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