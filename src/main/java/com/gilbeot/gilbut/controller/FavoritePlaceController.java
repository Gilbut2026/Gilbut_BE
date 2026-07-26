package com.gilbeot.gilbut.controller;

import com.gilbeot.gilbut.dto.user.request.FavoritePlaceCreateRequest;
import com.gilbeot.gilbut.dto.user.request.FavoritePlaceUpdateRequest;
import com.gilbeot.gilbut.dto.user.response.FavoritePlaceResponse;
import com.gilbeot.gilbut.global.common.api.ApiResponse;
import com.gilbeot.gilbut.global.common.code.SuccessCode;
import com.gilbeot.gilbut.service.user.FavoritePlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/favorites")
public class FavoritePlaceController {

    private final FavoritePlaceService favoritePlaceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoritePlaceResponse>>>
    getFavorites(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        List<FavoritePlaceResponse> response =
                favoritePlaceService.getFavorites(userId);

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FavoritePlaceResponse>>
    createFavorite(
            Authentication authentication,
            @Valid @RequestBody FavoritePlaceCreateRequest request
    ) {
        Long userId = extractUserId(authentication);

        FavoritePlaceResponse response =
                favoritePlaceService.createFavorite(
                        userId,
                        request
                );

        return ApiResponse.success(
                SuccessCode._CREATED,
                response
        );
    }

    @PatchMapping("/{favoriteId}")
    public ResponseEntity<ApiResponse<FavoritePlaceResponse>>
    updateFavorite(
            Authentication authentication,
            @PathVariable Long favoriteId,
            @Valid @RequestBody FavoritePlaceUpdateRequest request
    ) {
        Long userId = extractUserId(authentication);

        FavoritePlaceResponse response =
                favoritePlaceService.updateFavorite(
                        userId,
                        favoriteId,
                        request
                );

        return ApiResponse.success(
                SuccessCode._OK,
                response
        );
    }

    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<ApiResponse<Void>> deleteFavorite(
            Authentication authentication,
            @PathVariable Long favoriteId
    ) {
        Long userId = extractUserId(authentication);

        favoritePlaceService.deleteFavorite(
                userId,
                favoriteId
        );

        return ApiResponse.success(
                SuccessCode._OK,
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
            return Long.parseLong(authentication.getName());

        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "인증된 사용자 ID 형식이 올바르지 않습니다.",
                    e
            );
        }
    }
}