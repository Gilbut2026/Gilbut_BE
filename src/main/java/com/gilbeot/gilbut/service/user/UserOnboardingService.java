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

    private final UserAccessibilitySettingService accessibilitySettingService;
    private final UserMobilityProfileService mobilityProfileService;
    private final UserSettingsService userSettingsService;

    @Transactional
    public UserSettingsResponse completeOnboarding(
            Long userId,
            OnboardingSaveRequest request
    ) {
        accessibilitySettingService
                .saveVoiceGuidanceForOnboarding(
                        userId,
                        Boolean.TRUE.equals(
                                request.getVoiceGuidanceEnabled()
                        )
                );

        mobilityProfileService.saveMobilityProfile(
                userId,
                request.getWalkingDuration(),
                request.getStairLevel(),
                request.getSlopeLevel(),
                request.getRestStopPreference(),
                request.getTransferLevel(),
                request.getMobilityAid()
        );

        return userSettingsService.getSettings(userId);
    }
}