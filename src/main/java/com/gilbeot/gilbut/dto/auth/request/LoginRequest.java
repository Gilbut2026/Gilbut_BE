package com.gilbeot.gilbut.dto.auth.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    // 카카오 인가 서버로부터 프론트가 전달받은 인가코드(authorization code)
    private String code;
}
