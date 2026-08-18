package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.user.UserWithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
public class UserController {

    private final UserWithdrawalService userWithdrawalService;

    // 회원 탈퇴 — 계정과 딸린 자료(집 주소·즐겨찾기·비상 연락처·길찾기 기록·
    //            대화 세션·접근성 설정·이동 특성)를 모두 지운다

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> withdraw(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        userWithdrawalService.withdraw(userId);

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
