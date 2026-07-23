package com.gilbeot.gilbut.service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.dto.response.TokenResponse;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final AuthService authService;
    private final UserService userService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.client-secret:}")
    private String kakaoClientSecret; // Client Secret을 켠 경우에만 사용

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Transactional
    public TokenResponse authenticateUser(String code) {
        try {
            // 1) 인가 코드 → access_token 교환
            String kakaoAccessToken = requestKakaoAccessToken(code);

            // 2) access_token으로 사용자 정보 요청
            JsonNode root = requestKakaoUserInfo(kakaoAccessToken);

            if (!root.hasNonNull("id")) {
                throw new RuntimeException("카카오 응답에 id가 없습니다.");
            }
            String providerId = root.get("id").asText();

            JsonNode profile = root.path("kakao_account").path("profile");

            String avatarUrl = profile.hasNonNull("profile_image_url")
                    ? profile.get("profile_image_url").asText(null)
                    : null;

            // 닉네임 (선택 동의, 사용자가 거부했으면 없음 → username 자동생성으로 폴백)
            String nickname = profile.hasNonNull("nickname")
                    ? profile.get("nickname").asText(null)
                    : null;

            // 3) DB 저장 or 조회
            User user = userService.findByProviderAndProviderId("kakao", providerId)
                    .orElseGet(() -> userService.createUser("kakao", providerId, avatarUrl, nickname));

            // 4) 자체 JWT 발급
            return authService.issueTokens(user);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 오류 발생", e);
            throw new CustomException(ErrorCode.OAUTH_FAILED);
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

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);
        ResponseEntity<String> tokenResponse;
        try {
            tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, String.class);
        } catch (Exception e) {
            throw new RuntimeException("카카오 토큰 서버 요청 실패", e);
        }

        JsonNode tokenNode;
        try {
            tokenNode = objectMapper.readTree(tokenResponse.getBody());
        } catch (Exception e) {
            throw new RuntimeException("카카오 토큰 응답 파싱 실패", e);
        }

        if (!tokenNode.has("access_token")) {
            throw new RuntimeException("카카오 access_token 발급 실패: " + tokenResponse.getBody());
        }
        return tokenNode.get("access_token").asText();
    }

    private JsonNode requestKakaoUserInfo(String kakaoAccessToken) {
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(kakaoAccessToken);
        userHeaders.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<String> userResponse = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                String.class
        );

        try {
            return objectMapper.readTree(userResponse.getBody());
        } catch (Exception e) {
            throw new RuntimeException("카카오 사용자 정보 응답 파싱 실패", e);
        }
    }
}
