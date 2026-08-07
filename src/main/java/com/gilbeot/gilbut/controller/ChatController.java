package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.dto.chat.request.ChatMessageRequest;
import com.gilbeot.gilbut.dto.chat.request.PlaceConfirmationRequest;
import com.gilbeot.gilbut.dto.chat.request.DepartureTimeConfirmationRequest;
import com.gilbeot.gilbut.dto.chat.request.OriginConfirmationRequest;
import com.gilbeot.gilbut.dto.chat.response.ChatMessageResponse;
import com.gilbeot.gilbut.dto.chat.response.ChatSessionResponse;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.chat.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatMessageResponse>> chat(
            Authentication authentication,
            @Valid @RequestBody ChatMessageRequest request
    ) {
        Long userId = extractUserId(authentication);

        ChatMessageResponse response =
                chatService.chat(
                        userId,
                        request
                );

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    @PostMapping("/place-confirmation")
    public ResponseEntity<ApiResponse<ChatSessionResponse>> confirmDestination(
            Authentication authentication,
            @Valid @RequestBody PlaceConfirmationRequest request
    ) {
        Long userId = extractUserId(authentication);

        ChatSessionResponse response =
                chatService.confirmDestination(
                        userId,
                        request
                );

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    @PostMapping("/origin-confirmation")
    public ResponseEntity<ApiResponse<ChatSessionResponse>> confirmOrigin(
            Authentication authentication,
            @Valid @RequestBody OriginConfirmationRequest request
    ) {
        Long userId = extractUserId(authentication);

        ChatSessionResponse response =
                chatService.confirmOrigin(
                        userId,
                        request
                );

        return ApiResponse.success(
                SuccessCode._OK,
                response
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

    @PostMapping("/departure-time-confirmation")
    public ResponseEntity<ApiResponse<ChatSessionResponse>>
    confirmDepartureTime(
            Authentication authentication,
            @Valid @RequestBody
            DepartureTimeConfirmationRequest request
    ) {
        Long userId =
                extractUserId(authentication);

        ChatSessionResponse response =
                chatService.confirmDepartureTime(
                        userId,
                        request
                );

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }
}