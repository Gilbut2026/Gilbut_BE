package com.gilbeot.gilbut.service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.dto.auth.response.LoginResponse;
import com.gilbeot.gilbut.service.user.OnboardingStatusService;
import com.gilbeot.gilbut.dto.auth.response.TokenResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final AuthService authService;
    private final UserService userService;
    private final OnboardingStatusService onboardingStatusService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.client-secret:}")
    private String kakaoClientSecret;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Transactional
    public LoginResponse authenticateUser(String code) {
        try {
            // 1. 카카오 인가 코드로 카카오 액세스 토큰 발급
            String kakaoAccessToken =
                    requestKakaoAccessToken(code);

            // 2. 카카오 액세스 토큰으로 사용자 정보 조회
            JsonNode root =
                    requestKakaoUserInfo(kakaoAccessToken);

            if (!root.hasNonNull("id")) {
                throw new IllegalStateException(
                        "카카오 응답에 사용자 ID가 없습니다."
                );
            }

            String providerId =
                    root.get("id").asText();

            JsonNode profile = root
                    .path("kakao_account")
                    .path("profile");

            // 닉네임 동의를 받지 않았거나 값이 없으면
            // UserService에서 자동 생성
            String nickname =
                    profile.hasNonNull("nickname")
                            ? profile.get("nickname").asText()
                            : null;

            // 3. 기존 사용자 여부 확인
            Optional<User> existingUser =
                    userService.findByProviderId(providerId);

            boolean newUser =
                    existingUser.isEmpty();

            // 4. 기존 사용자 조회 또는 신규 사용자 생성
            User user = existingUser.orElseGet(() ->
                    userService.createUser(
                            providerId,
                            nickname
                    )
            );

            // 5. JWT 발급
            TokenResponse tokenResponse =
                    authService.issueTokens(user);

            // 6. 이동 프로필 존재 여부로 온보딩 완료 판단
            boolean onboardingCompleted =
                    onboardingStatusService.isCompleted(
                            user.getId()
                    );

            // 7. 로그인 전용 응답 반환
            return LoginResponse.of(
                    tokenResponse,
                    newUser,
                    onboardingCompleted
            );

        } catch (CustomException e) {
            throw e;

        } catch (Exception e) {
            log.error(
                    "카카오 로그인 처리 중 오류 발생",
                    e
            );

            throw new CustomException(
                    ErrorCode.OAUTH_FAILED
            );
        }
    }

    private String requestKakaoAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);

        if (StringUtils.hasText(kakaoClientSecret)) {
            params.add("client_secret", kakaoClientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        ResponseEntity<String> response;

        try {
            response = restTemplate.postForEntity(
                    tokenUrl,
                    request,
                    String.class
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "카카오 토큰 서버 요청에 실패했습니다.",
                    e
            );
        }

        JsonNode tokenNode;

        try {
            tokenNode = objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "카카오 토큰 응답 파싱에 실패했습니다.",
                    e
            );
        }

        if (!tokenNode.hasNonNull("access_token")) {
            throw new IllegalStateException(
                    "카카오 액세스 토큰 발급에 실패했습니다."
            );
        }

        return tokenNode.get("access_token").asText();
    }

    private JsonNode requestKakaoUserInfo(String kakaoAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(kakaoAccessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response;

        try {
            response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "카카오 사용자 정보 요청에 실패했습니다.",
                    e
            );
        }

        try {
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "카카오 사용자 정보 응답 파싱에 실패했습니다.",
                    e
            );
        }
    }
}