package com.gilbeot.gilbut.service.user;

import com.gilbeot.gilbut.dto.user.request.OnboardingSaveRequest;
import com.gilbeot.gilbut.dto.user.response.UserSettingsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserOnboardingService {

    private final UserAccessibilitySettingService
            accessibilitySettingService;

    private final UserMobilityProfileService
            mobilityProfileService;

    private final UserSettingsService
            userSettingsService;

    @Transactional
    public UserSettingsResponse completeOnboarding(
            Long userId,
            OnboardingSaveRequest request
    ) {
        // 음성 안내는 접근성 설정 테이블에 저장
        accessibilitySettingService
                .saveVoiceGuidanceForOnboarding(
                        userId,
                        Boolean.TRUE.equals(
                                request.getVoiceGuidanceEnabled()
                        )
                );

        // 이동 관련 값은 이동 설정 테이블에 저장
        mobilityProfileService.saveMobilityProfile(
                userId,
                request.getWalkingDuration(),
                request.getStairLevel(),
                request.getRestStopPreference(),
                request.getTransferLevel(),
                request.getMobilityAid()
        );

        // 저장 완료 후 통합 설정 응답 반환
        return userSettingsService.getSettings(userId);
    }
}