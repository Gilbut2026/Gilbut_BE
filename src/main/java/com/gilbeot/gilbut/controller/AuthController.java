package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.dto.auth.request.LoginRequest;
import com.gilbeot.gilbut.dto.auth.request.TokenRequest;
import com.gilbeot.gilbut.dto.auth.response.TokenResponse;
import com.gilbeot.gilbut.dto.auth.response.LoginResponse;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.auth.AuthService;
import com.gilbeot.gilbut.service.auth.KakaoOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final AuthService authService;

    // 카카오 로그인 (프론트에서 받은 인가코드를 넘겨받아 처리)
    @PostMapping("/kakao-login")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(
            @RequestBody LoginRequest request
    ) {
        LoginResponse response =
                kakaoOAuthService.authenticateUser(
                        request.getCode()
                );

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    // refresh token으로 access token 재발급
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@RequestBody TokenRequest request) {
        TokenResponse response = authService.refresh(request.getRefreshToken());
        return ApiResponse.success(SuccessCode._OK, response);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        authService.logout(userId);
        return ApiResponse.success(SuccessCode._NO_CONTENT, null);
    }
}
