package com.gilbeot.gilbut.dto.route.walking.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WalkingRouteRequest {

    @Valid
    @NotNull
    private RoutePlaceRequest origin;

    @Valid
    @NotNull
    private RoutePlaceRequest destination;

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

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        private Double latitude;

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        private Double longitude;
    }
}
