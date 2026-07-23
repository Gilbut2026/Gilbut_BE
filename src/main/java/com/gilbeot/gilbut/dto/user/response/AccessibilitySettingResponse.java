package com.gilbeot.gilbut.dto.user.response;

import com.gilbeot.gilbut.domain.user.UserAccessibilitySetting;
import com.gilbeot.gilbut.domain.user.type.FontSize;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AccessibilitySettingResponse {

    private Long id;

    private boolean voiceGuidanceEnabled;

    private boolean highContrastEnabled;

    private FontSize fontSize;

    private BigDecimal voiceSpeed;

    public static AccessibilitySettingResponse from(
            UserAccessibilitySetting setting
    ) {
        return AccessibilitySettingResponse.builder()
                .id(setting.getId())
                .voiceGuidanceEnabled(
                        setting.isVoiceGuidanceEnabled()
                )
                .highContrastEnabled(
                        setting.isHighContrastEnabled()
                )
                .fontSize(setting.getFontSize())
                .voiceSpeed(setting.getVoiceSpeed())
                .build();
    }
}