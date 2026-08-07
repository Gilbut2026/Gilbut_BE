package com.gilbeot.gilbut.dto.station.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AlongRouteStationElevatorRequest {

    @Valid
    @NotEmpty(message = "경로 좌표는 필수입니다.")
    @Size(
            min = 2,
            max = 1000,
            message = "경로 좌표는 2개 이상 1000개 이하로 전달해야 합니다."
    )
    private List<RoutePointRequest> routePoints;

    private Integer radiusMeters;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RoutePointRequest {

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
