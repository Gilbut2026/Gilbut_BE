package com.gilbeot.gilbut.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI gilbutAPI() {

        // API 문서 정보 설정
        Info info = new Info()
                .title("Gilbut API")
                .description("길벗 API 명세서")
                .version("1.0.0");

        // Security Scheme 정의 (JWT 토큰 기반 인증)
        SecurityScheme jwtScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)  // HTTP 기반 인증
                .scheme("bearer")  // Bearer 방식 사용
                .bearerFormat("JWT");  // JWT 사용 명시

        return new OpenAPI()
                .addServersItem(new Server().url("/"))  // 기본 서버 설정
                .info(info)  // API 문서 정보 추가
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))  // JWT 인증 적용
                .components(new Components().addSecuritySchemes("BearerAuth", jwtScheme)); // 보안 스키마 설정
    }
}
