package com.gilbeot.gilbut.service.user;

import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.domain.user.UserMobilityProfile;
import com.gilbeot.gilbut.domain.user.type.MobilityAid;
import com.gilbeot.gilbut.domain.user.type.RestStopPreference;
import com.gilbeot.gilbut.domain.user.type.SlopeLevel;
import com.gilbeot.gilbut.domain.user.type.StairLevel;
import com.gilbeot.gilbut.domain.user.type.TransferLevel;
import com.gilbeot.gilbut.domain.user.type.WalkingDuration;
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
    private final UserMobilityProfileRepository mobilityProfileRepository;

    public MobilityProfileResponse getMobilityProfile(
            Long userId
    ) {
        UserMobilityProfile profile = mobilityProfileRepository
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
        return saveMobilityProfile(
                userId,
                request.getWalkingDuration(),
                request.getStairLevel(),
                request.getSlopeLevel(),
                request.getRestStopPreference(),
                request.getTransferLevel(),
                request.getMobilityAid()
        );
    }

    @Transactional
    public MobilityProfileResponse saveMobilityProfile(
            Long userId,
            WalkingDuration walkingDuration,
            StairLevel stairLevel,
            SlopeLevel slopeLevel,
            RestStopPreference restStopPreference,
            TransferLevel transferLevel,
            MobilityAid mobilityAid
    ) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        UserMobilityProfile profile = mobilityProfileRepository
                .findByUserId(userId)
                .orElseGet(() ->
                        createProfile(
                                user,
                                walkingDuration,
                                stairLevel,
                                slopeLevel,
                                restStopPreference,
                                transferLevel,
                                mobilityAid
                        )
                );

        profile.update(
                walkingDuration,
                stairLevel,
                slopeLevel,
                restStopPreference,
                transferLevel,
                mobilityAid
        );

        UserMobilityProfile savedProfile =
                mobilityProfileRepository.save(profile);

        return MobilityProfileResponse.from(savedProfile);
    }

    private UserMobilityProfile createProfile(
            User user,
            WalkingDuration walkingDuration,
            StairLevel stairLevel,
            SlopeLevel slopeLevel,
            RestStopPreference restStopPreference,
            TransferLevel transferLevel,
            MobilityAid mobilityAid
    ) {
        return UserMobilityProfile.builder()
                .user(user)
                .walkingDuration(walkingDuration)
                .stairLevel(stairLevel)
                .slopeLevel(slopeLevel)
                .restStopPreference(restStopPreference)
                .transferLevel(transferLevel)
                .mobilityAid(mobilityAid)
                .build();
    }
}