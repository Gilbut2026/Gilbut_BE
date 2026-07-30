package com.gilbeot.gilbut.dto.place.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HomePlaceSaveRequest {

    @NotBlank(message = "주소 정보가 필요합니다.")
    @Size(
            max = 255,
            message = "주소는 255자 이하이어야 합니다."
    )
    private String address;

    @NotNull(message = "위도 정보가 필요합니다.")
    @DecimalMin(
            value = "-90.0",
            message = "위도는 -90 이상이어야 합니다."
    )
    @DecimalMax(
            value = "90.0",
            message = "위도는 90 이하이어야 합니다."
    )
    private Double latitude;

    @NotNull(message = "경도 정보가 필요합니다.")
    @DecimalMin(
            value = "-180.0",
            message = "경도는 -180 이상이어야 합니다."
    )
    @DecimalMax(
            value = "180.0",
            message = "경도는 180 이하이어야 합니다."
    )
    private Double longitude;
}