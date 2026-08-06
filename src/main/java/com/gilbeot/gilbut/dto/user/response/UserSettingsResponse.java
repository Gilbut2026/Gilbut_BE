package com.gilbeot.gilbut.dto.user.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSettingsResponse {

    // 필수 이동 설정 온보딩 완료 여부
    private boolean onboardingCompleted;

    // 경로 추천 기준
    private MobilityProfileResponse mobilityProfile;

    // 보기와 듣기
    private AccessibilitySettingResponse accessibilitySettings;

    // 내 정보와 안전
    private SafetySummary safety;

    @Getter
    @Builder
    public static class SafetySummary {

        private String homeAddress;

        private long emergencyContactCount;
    }
}