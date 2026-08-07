package com.gilbeot.gilbut.dto.chat.request;

import com.gilbeot.gilbut.domain.chat.OriginType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OriginConfirmationRequest {

    @NotNull(message = "출발지 유형은 필수입니다.")
    private OriginType originType;

    @Size(
            max = 100,
            message = "장소 ID는 100자 이하이어야 합니다."
    )
    private String placeId;

    @Size(
            max = 100,
            message = "장소명은 100자 이하이어야 합니다."
    )
    private String name;

    @Size(
            max = 255,
            message = "주소는 255자 이하이어야 합니다."
    )
    private String address;

    @DecimalMin(
            value = "-90.0",
            message = "위도는 -90 이상이어야 합니다."
    )
    @DecimalMax(
            value = "90.0",
            message = "위도는 90 이하여야 합니다."
    )
    private Double latitude;

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