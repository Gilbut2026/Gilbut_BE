package com.gilbeot.gilbut.dto.chat.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DepartureTimeConfirmationRequest {

    @NotNull(message = "출발 시간은 필수입니다.")
    private LocalDateTime departureDateTime;
}