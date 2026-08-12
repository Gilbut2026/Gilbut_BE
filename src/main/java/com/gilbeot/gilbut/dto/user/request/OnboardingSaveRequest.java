package com.gilbeot.gilbut.dto.user.request;

import com.gilbeot.gilbut.domain.user.type.MobilityAid;
import com.gilbeot.gilbut.domain.user.type.RestStopPreference;
import com.gilbeot.gilbut.domain.user.type.SlopeLevel;
import com.gilbeot.gilbut.domain.user.type.StairLevel;
import com.gilbeot.gilbut.domain.user.type.TransferLevel;
import com.gilbeot.gilbut.domain.user.type.WalkingDuration;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OnboardingSaveRequest {

    @NotNull(
            message = "음성 안내 사용 여부를 선택해 주세요."
    )
    private Boolean voiceGuidanceEnabled;

    @NotNull(
            message = "한 번에 걸을 수 있는 시간을 선택해 주세요."
    )
    private WalkingDuration walkingDuration;

    @NotNull(
            message = "계단 이용 가능 정도를 선택해 주세요."
    )
    private StairLevel stairLevel;

    @NotNull(
            message = "오르막길 이동 가능 정도를 선택해 주세요."
    )
    private SlopeLevel slopeLevel;

    @NotNull(
            message = "쉬어 갈 곳 필요 여부를 선택해 주세요."
    )
    private RestStopPreference restStopPreference;

    @NotNull(
            message = "환승 선호 정도를 선택해 주세요."
    )
    private TransferLevel transferLevel;

    @NotNull(
            message = "이동 보조기구를 선택해 주세요."
    )
    private MobilityAid mobilityAid;
}