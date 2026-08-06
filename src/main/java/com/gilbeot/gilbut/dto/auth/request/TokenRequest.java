package com.gilbeot.gilbut.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequest {

    @NotBlank(
            message = "리프레시 토큰이 필요합니다."
    )
    private String refreshToken;
}