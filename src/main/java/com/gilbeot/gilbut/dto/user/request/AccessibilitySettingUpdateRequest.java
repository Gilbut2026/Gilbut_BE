package com.gilbeot.gilbut.dto.user.request;

import com.gilbeot.gilbut.domain.user.type.FontSize;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class AccessibilitySettingUpdateRequest {

    @NotNull(
            message = "음성 안내 사용 여부를 선택해 주세요."
    )
    private Boolean voiceGuidanceEnabled;

    @NotNull(
            message = "고대비 화면 사용 여부를 선택해 주세요."
    )
    private Boolean highContrastEnabled;

    @NotNull(
            message = "글자 크기를 선택해 주세요."
    )
    private FontSize fontSize;

    @NotNull(
            message = "음성 안내 속도를 입력해 주세요."
    )
    @DecimalMin(
            value = "0.7",
            inclusive = true,
            message = "음성 안내 속도는 0.7 이상이어야 합니다."
    )
    @DecimalMax(
            value = "1.4",
            inclusive = true,
            message = "음성 안내 속도는 1.4 이하여야 합니다."
    )
    @Digits(
            integer = 1,
            fraction = 2,
            message = "음성 안내 속도는 소수점 둘째 자리까지 입력할 수 있습니다."
    )
    private BigDecimal voiceSpeed;
}