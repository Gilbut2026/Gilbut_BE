package com.gilbeot.gilbut.service.user;

import com.gilbeot.gilbut.dto.place.response.HomePlaceResponse;
import com.gilbeot.gilbut.dto.user.response.AccessibilitySettingResponse;
import com.gilbeot.gilbut.dto.user.response.MobilityProfileResponse;
import com.gilbeot.gilbut.dto.user.response.UserSettingsResponse;
import com.gilbeot.gilbut.repository.EmergencyContactRepository;
import com.gilbeot.gilbut.repository.UserMobilityProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSettingsService {

    private final UserMobilityProfileRepository mobilityProfileRepository;
    private final UserAccessibilitySettingService accessibilitySettingService;
    private final HomePlaceService homePlaceService;
    private final EmergencyContactRepository emergencyContactRepository;
    private final OnboardingStatusService onboardingStatusService;
    public UserSettingsResponse getSettings(Long userId) {

        // 경로 추천 기준
        MobilityProfileResponse mobilityProfile =
                mobilityProfileRepository.findByUserId(userId)
                        .map(MobilityProfileResponse::from)
                        .orElse(null);

        // 보기와 듣기
        AccessibilitySettingResponse accessibilitySettings =
                accessibilitySettingService.getAccessibilitySetting(userId);

        // 집 주소
        HomePlaceResponse home =
                homePlaceService.getHome(userId);

        // 비상 연락처 개수
        long emergencyContactCount =
                emergencyContactRepository.countByUserId(userId);

        UserSettingsResponse.SafetySummary safety =
                UserSettingsResponse.SafetySummary.builder()
                        .homeAddress(
                                home == null
                                        ? null
                                        : home.getAddress()
                        )
                        .emergencyContactCount(emergencyContactCount)
                        .build();

        return UserSettingsResponse.builder()
                .onboardingCompleted(
                        onboardingStatusService.isCompleted(userId)
                )
                .mobilityProfile(mobilityProfile)
                .accessibilitySettings(accessibilitySettings)
                .safety(safety)
                .build();
    }
}