package com.gilbeot.gilbut.service.user;

import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.domain.user.UserMobilityProfile;
import com.gilbeot.gilbut.dto.user.request.MobilityProfileSaveRequest;
import com.gilbeot.gilbut.dto.user.response.MobilityProfileResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.repository.UserMobilityProfileRepository;
import com.gilbeot.gilbut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserMobilityProfileService {

    private final UserRepository userRepository;

    private final UserMobilityProfileRepository
            mobilityProfileRepository;

    public MobilityProfileResponse getMobilityProfile(
            Long userId
    ) {
        UserMobilityProfile profile =
                mobilityProfileRepository
                        .findByUserId(userId)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.MOBILITY_PROFILE_NOT_FOUND
                                )
                        );

        return MobilityProfileResponse.from(profile);
    }


    @Transactional
    public MobilityProfileResponse saveMobilityProfile(
            Long userId,
            MobilityProfileSaveRequest request
    ) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        UserMobilityProfile profile =
                mobilityProfileRepository
                        .findByUserId(userId)
                        .orElseGet(() ->
                                createProfile(user, request)
                        );

        profile.update(
                request.getWalkingDuration(),
                request.getStairLevel(),
                request.getRestStopPreference(),
                request.getTransferLevel(),
                request.getMobilityAid()
        );

        UserMobilityProfile savedProfile =
                mobilityProfileRepository.save(profile);

        return MobilityProfileResponse.from(savedProfile);
    }


    private UserMobilityProfile createProfile(
            User user,
            MobilityProfileSaveRequest request
    ) {
        return UserMobilityProfile.builder()
                .user(user)
                .walkingDuration(
                        request.getWalkingDuration()
                )
                .stairLevel(
                        request.getStairLevel()
                )
                .restStopPreference(
                        request.getRestStopPreference()
                )
                .transferLevel(
                        request.getTransferLevel()
                )
                .mobilityAid(
                        request.getMobilityAid()
                )
                .build();
    }
}