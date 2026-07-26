package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.dto.user.request.EmergencyContactSaveRequest;
import com.gilbeot.gilbut.dto.user.response.EmergencyContactResponse;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.user.EmergencyContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/emergency-contacts")
public class EmergencyContactController {

    private final EmergencyContactService emergencyContactService;

    // 비상 연락처 목록 조회
    @GetMapping
    public ResponseEntity<
            ApiResponse<List<EmergencyContactResponse>>
            > getEmergencyContacts(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        List<EmergencyContactResponse> response =
                emergencyContactService
                        .getEmergencyContacts(userId);

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    // 비상 연락처 등록
    @PostMapping
    public ResponseEntity<
            ApiResponse<EmergencyContactResponse>
            > createEmergencyContact(
            Authentication authentication,
            @Valid @RequestBody
            EmergencyContactSaveRequest request
    ) {
        Long userId = extractUserId(authentication);

        EmergencyContactResponse response =
                emergencyContactService
                        .createEmergencyContact(
                                userId,
                                request
                        );

        return ApiResponse.success(
                SuccessCode._CREATED,
                response
        );
    }

    // 비상 연락처 수정
    @PutMapping("/{contactId}")
    public ResponseEntity<
            ApiResponse<EmergencyContactResponse>
            > updateEmergencyContact(
            Authentication authentication,
            @PathVariable Long contactId,
            @Valid @RequestBody
            EmergencyContactSaveRequest request
    ) {
        Long userId = extractUserId(authentication);

        EmergencyContactResponse response =
                emergencyContactService
                        .updateEmergencyContact(
                                userId,
                                contactId,
                                request
                        );

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    // 비상 연락처 삭제
    @DeleteMapping("/{contactId}")
    public ResponseEntity<ApiResponse<Void>>
    deleteEmergencyContact(
            Authentication authentication,
            @PathVariable Long contactId
    ) {
        Long userId = extractUserId(authentication);

        emergencyContactService.deleteEmergencyContact(
                userId,
                contactId
        );

        return ApiResponse.success(
                SuccessCode._NO_CONTENT,
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