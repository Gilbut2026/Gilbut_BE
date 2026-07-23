package com.gilbeot.gilbut.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    // access 토큰 유효 기간 1시간
    private final long accessTokenValidity = 3600000L;
    // refresh 토큰 유효 기간 7일
    private final long refreshTokenValidity = 604800000L;

    // access 토큰 생성
    public String generateAccessToken(String userId) {
        return generateToken(userId, accessTokenValidity);
    }

    // refresh 토큰 생성
    public String generateRefreshToken(String userId) {
        return generateToken(userId, refreshTokenValidity);
    }

    // JWT 토큰 생성
    private String generateToken(String userId, long validity) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 사용자 ID 추출
    public Long getUserIdFromToken(String token) {
        String subject = parseClaims(token).getSubject();
        return Long.parseLong(subject);
    }

    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        try {
            return parseClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            // 토큰 만료
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰 검증 실패
        }
        return false;
    }

    // JWT 만료 시간 추출
    public LocalDateTime getExpiration(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // JWT parsing
    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
