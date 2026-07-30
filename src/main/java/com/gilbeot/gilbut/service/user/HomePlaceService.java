package com.gilbeot.gilbut.service.user;

import com.gilbeot.gilbut.domain.home.HomePlace;
import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.dto.place.request.HomePlaceSaveRequest;
import com.gilbeot.gilbut.dto.place.response.HomePlaceResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.repository.HomePlaceRepository;
import com.gilbeot.gilbut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomePlaceService {

    private final UserRepository userRepository;
    private final HomePlaceRepository homePlaceRepository;

    // 집 주소 조회
    public HomePlaceResponse getHome(Long userId) {

        return homePlaceRepository.findByUserId(userId)
                .map(HomePlaceResponse::from)
                .orElse(null);
    }

    // 집 주소 등록 또는 수정
    @Transactional
    public HomePlaceResponse saveHome(
            Long userId,
            HomePlaceSaveRequest request
    ) {

        HomePlace homePlace = homePlaceRepository
                .findByUserId(userId)
                .orElseGet(() -> {

                    User user = userRepository.findById(userId)
                            .orElseThrow(() ->
                                    new CustomException(
                                            ErrorCode.USER_NOT_FOUND
                                    )
                            );

                    return HomePlace.builder()
                            .user(user)
                            .address(request.getAddress().trim())
                            .latitude(request.getLatitude())
                            .longitude(request.getLongitude())
                            .build();
                });

        // 기존 집 주소가 있는 경우 수정
        if (homePlace.getId() != null) {
            homePlace.update(
                    request.getAddress(),
                    request.getLatitude(),
                    request.getLongitude()
            );
        }

        HomePlace savedHomePlace =
                homePlaceRepository.save(homePlace);

        return HomePlaceResponse.from(savedHomePlace);
    }

    // 집 주소 삭제
    @Transactional
    public void deleteHome(Long userId) {

        HomePlace homePlace = homePlaceRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.HOME_PLACE_NOT_FOUND
                        )
                );

        homePlaceRepository.delete(homePlace);
    }
}