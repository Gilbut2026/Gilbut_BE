package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.dto.chat.response.ChatSessionResponse;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.chat.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/session")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    @GetMapping
    public ResponseEntity<ApiResponse<ChatSessionResponse>> getSession(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        ChatSessionResponse response =
                chatSessionService.getOrCreateSession(userId);

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<ChatSessionResponse>> resetSession(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        ChatSessionResponse response =
                chatSessionService.resetSession(userId);

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