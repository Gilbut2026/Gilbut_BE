package com.gilbeot.gilbut.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/auth/kakao-login",
            "/api/auth/refresh",
            "/swagger-ui",     // Swagger
            "/v3/api-docs"     // OpenAPI docs
    );

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String path = request.getRequestURI();

        // 특정 경로는 필터 적용 안 함
        if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1) Authorization 헤더에서 토큰 추출
            String token = getTokenFromRequest(request);

            // 토큰이 없으면 그냥 익명 사용자로 통과
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2) 토큰 유효성 검증
            if (!jwtTokenProvider.validateToken(token)) {
                throw new CustomException(ErrorCode.JWT_TOKEN_INVALID);
            }

            // 3) 토큰에서 userId 추출
            Long userId = jwtTokenProvider.getUserIdFromToken(token);

            // 4) DB에서 유저 조회
            User user = userService.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            // 5) CustomUserDetails 생성
            CustomUserDetails principal = new CustomUserDetails(user);

            // 6) 인증 객체 생성 및 SecurityContext에 저장
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities()
            );
            ((UsernamePasswordAuthenticationToken) authentication)
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (CustomException e) {
            log.error("JWT 인증 실패: {}", e.getMessage());
            response.setStatus(e.getErrorCode().getStatus().value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(),
                    ApiResponse.fail(e.getErrorCode()).getBody());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
