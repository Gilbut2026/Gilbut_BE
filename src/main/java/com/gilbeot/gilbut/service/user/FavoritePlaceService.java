package com.gilbeot.gilbut.service.user;

import com.gilbeot.gilbut.domain.user.FavoritePlace;
import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.dto.user.request.FavoritePlaceCreateRequest;
import com.gilbeot.gilbut.dto.user.request.FavoritePlaceUpdateRequest;
import com.gilbeot.gilbut.dto.user.response.FavoritePlaceResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.repository.FavoritePlaceRepository;
import com.gilbeot.gilbut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoritePlaceService {

    private final UserRepository userRepository;
    private final FavoritePlaceRepository favoritePlaceRepository;

    // 즐겨찾기 장소 목록 조회
    public List<FavoritePlaceResponse> getFavorites(Long userId) {
        return favoritePlaceRepository
                .findAllByUserIdOrderByIdDesc(userId)
                .stream()
                .map(FavoritePlaceResponse::from)
                .toList();
    }

    // 즐겨찾기 장소 등록
    @Transactional
    public FavoritePlaceResponse createFavorite(
            Long userId,
            FavoritePlaceCreateRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        FavoritePlace favoritePlace = FavoritePlace.builder()
                .user(user)
                .name(request.getName().trim())
                .address(request.getAddress().trim())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        FavoritePlace saved =
                favoritePlaceRepository.save(favoritePlace);

        return FavoritePlaceResponse.from(saved);
    }

    // 즐겨찾기 장소 이름 수정
    @Transactional
    public FavoritePlaceResponse updateFavorite(
            Long userId,
            Long favoriteId,
            FavoritePlaceUpdateRequest request
    ) {
        FavoritePlace favoritePlace =
                favoritePlaceRepository
                        .findByIdAndUserId(favoriteId, userId)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.FAVORITE_PLACE_NOT_FOUND
                                )
                        );

        favoritePlace.updateName(request.getName());

        return FavoritePlaceResponse.from(favoritePlace);
    }

    // 즐겨찾기 장소 삭제
    @Transactional
    public void deleteFavorite(
            Long userId,
            Long favoriteId
    ) {
        FavoritePlace favoritePlace =
                favoritePlaceRepository
                        .findByIdAndUserId(favoriteId, userId)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.FAVORITE_PLACE_NOT_FOUND
                                )
                        );

        favoritePlaceRepository.delete(favoritePlace);
    }
}