package com.gilbeot.gilbut.dto.user.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmergencyContactSaveRequest {

    @NotBlank(message = "비상 연락처 이름을 입력해 주세요.")
    @Size(
            max = 30,
            message = "비상 연락처 이름은 30자 이하로 입력해 주세요."
    )
    private String name;

    @NotBlank(message = "관계를 입력해 주세요.")
    @Size(
            max = 30,
            message = "관계는 30자 이하로 입력해 주세요."
    )
    private String relationship;

    @NotBlank(message = "전화번호를 입력해 주세요.")
    @Pattern(
            regexp = "^[0-9-]{8,20}$",
            message = "전화번호 형식이 올바르지 않습니다."
    )
    private String phoneNumber;
}