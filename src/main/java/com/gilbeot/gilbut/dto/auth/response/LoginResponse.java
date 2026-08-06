package com.gilbeot.gilbut.dto.auth.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;

    private String refreshToken;

    private boolean newUser;

    private boolean onboardingCompleted;

    public static LoginResponse of(
            TokenResponse tokenResponse,
            boolean newUser,
            boolean onboardingCompleted
    ) {
        return LoginResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .newUser(newUser)
                .onboardingCompleted(onboardingCompleted)
                .build();
    }
}