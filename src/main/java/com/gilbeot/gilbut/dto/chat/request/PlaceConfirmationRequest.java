package com.gilbeot.gilbut.dto.chat.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceConfirmationRequest {

    @NotBlank(message = "장소 ID는 필수입니다.")
    private String placeId;

    @NotBlank(message = "장소명은 필수입니다.")
    private String name;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @NotNull(message = "위도는 필수입니다.")
    @DecimalMin(
            value = "-90.0",
            message = "위도는 -90 이상이어야 합니다."
    )
    @DecimalMax(
            value = "90.0",
            message = "위도는 90 이하여야 합니다."
    )
    private Double latitude;

    @NotNull(message = "경도는 필수입니다.")
    @DecimalMin(
            value = "-180.0",
            message = "경도는 -180 이상이어야 합니다."
    )
    @DecimalMax(
            value = "180.0",
            message = "경도는 180 이하여야 합니다."
    )
    private Double longitude;
}