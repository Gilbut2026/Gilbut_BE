package com.gilbeot.gilbut.dto.chat.request;

import com.gilbeot.gilbut.domain.chat.TodayCondition;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TodayConditionConfirmationRequest {

    @NotNull(message = "오늘 이동 상태는 필수입니다.")
    private TodayCondition todayCondition;
}