package com.gilbeot.gilbut.service.user;

import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.domain.user.UserAccessibilitySetting;
import com.gilbeot.gilbut.domain.user.type.FontSize;
import com.gilbeot.gilbut.dto.user.request.AccessibilitySettingUpdateRequest;
import com.gilbeot.gilbut.dto.user.response.AccessibilitySettingResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.repository.UserAccessibilitySettingRepository;
import com.gilbeot.gilbut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAccessibilitySettingService {

    private static final BigDecimal DEFAULT_VOICE_SPEED =
            new BigDecimal("1.00");

    private final UserRepository userRepository;

    private final UserAccessibilitySettingRepository
            accessibilitySettingRepository;

    public AccessibilitySettingResponse getAccessibilitySetting(
            Long userId
    ) {
        User user = findUser(userId);

        return accessibilitySettingRepository
                .findByUserId(userId)
                .map(AccessibilitySettingResponse::from)
                .orElseGet(() ->
                        AccessibilitySettingResponse.from(
                                createDefaultSetting(user)
                        )
                );
    }

    @Transactional
    public AccessibilitySettingResponse saveAccessibilitySetting(
            Long userId,
            AccessibilitySettingUpdateRequest request
    ) {
        User user = findUser(userId);

        UserAccessibilitySetting setting =
                accessibilitySettingRepository
                        .findByUserId(userId)
                        .orElseGet(() ->
                                createSetting(user, request)
                        );

        setting.update(
                Boolean.TRUE.equals(
                        request.getVoiceGuidanceEnabled()
                ),
                Boolean.TRUE.equals(
                        request.getHighContrastEnabled()
                ),
                request.getFontSize(),
                request.getVoiceSpeed()
        );

        UserAccessibilitySetting savedSetting =
                accessibilitySettingRepository.save(setting);

        return AccessibilitySettingResponse.from(savedSetting);
    }

    @Transactional
    public AccessibilitySettingResponse saveVoiceGuidanceForOnboarding(
            Long userId,
            boolean voiceGuidanceEnabled
    ) {
        User user = findUser(userId);

        UserAccessibilitySetting setting =
                accessibilitySettingRepository
                        .findByUserId(userId)
                        .orElseGet(() ->
                                createDefaultSetting(user)
                        );

        setting.updateVoiceGuidance(
                voiceGuidanceEnabled
        );

        UserAccessibilitySetting savedSetting =
                accessibilitySettingRepository.save(setting);

        return AccessibilitySettingResponse.from(savedSetting);
    }

    private User findUser(
            Long userId
    ) {
        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }

    private UserAccessibilitySetting createDefaultSetting(
            User user
    ) {
        return UserAccessibilitySetting.builder()
                .user(user)
                .voiceGuidanceEnabled(false)
                .highContrastEnabled(false)
                .fontSize(FontSize.NORMAL)
                .voiceSpeed(DEFAULT_VOICE_SPEED)
                .build();
    }

    private UserAccessibilitySetting createSetting(
            User user,
            AccessibilitySettingUpdateRequest request
    ) {
        return UserAccessibilitySetting.builder()
                .user(user)
                .voiceGuidanceEnabled(
                        Boolean.TRUE.equals(
                                request.getVoiceGuidanceEnabled()
                        )
                )
                .highContrastEnabled(
                        Boolean.TRUE.equals(
                                request.getHighContrastEnabled()
                        )
                )
                .fontSize(
                        request.getFontSize()
                )
                .voiceSpeed(
                        request.getVoiceSpeed()
                )
                .build();
    }
}