package com.gilbeot.gilbut.dto.route.transit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TransitRouteRequest {

    @Valid
    @NotNull(message = "출발지 정보는 필수입니다.")
    private RoutePlaceRequest origin;

    @Valid
    @NotNull(message = "목적지 정보는 필수입니다.")
    private RoutePlaceRequest destination;

    @NotNull(message = "출발 시간은 필수입니다.")
    private LocalDateTime departureDateTime;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RoutePlaceRequest {

        @Size(max = 100)
        private String placeId;

        @Size(max = 100)
        private String name;

        @Size(max = 300)
        private String address;

        @NotNull(message = "위도는 필수입니다.")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
        private Double latitude;

        @NotNull(message = "경도는 필수입니다.")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
        private Double longitude;
    }
}
