package com.gilbeot.gilbut.dto.user.response;

import com.gilbeot.gilbut.domain.user.UserMobilityProfile;
import com.gilbeot.gilbut.domain.user.type.MobilityAid;
import com.gilbeot.gilbut.domain.user.type.RestStopPreference;
import com.gilbeot.gilbut.domain.user.type.SlopeLevel;
import com.gilbeot.gilbut.domain.user.type.StairLevel;
import com.gilbeot.gilbut.domain.user.type.TransferLevel;
import com.gilbeot.gilbut.domain.user.type.WalkingDuration;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MobilityProfileResponse {

    private Long id;

    private WalkingDuration walkingDuration;

    private StairLevel stairLevel;

    private SlopeLevel slopeLevel;

    private RestStopPreference restStopPreference;

    private TransferLevel transferLevel;

    private MobilityAid mobilityAid;

    public static MobilityProfileResponse from(
            UserMobilityProfile profile
    ) {
        return MobilityProfileResponse.builder()
                .id(profile.getId())
                .walkingDuration(
                        profile.getWalkingDuration()
                )
                .stairLevel(
                        profile.getStairLevel()
                )
                .slopeLevel(
                        profile.getSlopeLevel()
                )
                .restStopPreference(
                        profile.getRestStopPreference()
                )
                .transferLevel(
                        profile.getTransferLevel()
                )
                .mobilityAid(
                        profile.getMobilityAid()
                )
                .build();
    }
}