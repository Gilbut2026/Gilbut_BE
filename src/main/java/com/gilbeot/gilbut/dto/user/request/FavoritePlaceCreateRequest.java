package com.gilbeot.gilbut.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FavoritePlaceCreateRequest {

    @NotBlank(message = "장소명 정보가 필요합니다.")
    @Size(
            max = 100,
            message = "장소명은 100자 이하이어야 합니다."
    )
    private String name;

    @NotBlank(message = "주소 정보가 필요합니다.")
    @Size(
            max = 255,
            message = "주소는 255자 이하이어야 합니다."
    )
    private String address;

    @NotNull(message = "위도 정보가 필요합니다.")
    private Double latitude;

    @NotNull(message = "경도 정보가 필요합니다.")
    private Double longitude;
}