package com.gilbeot.gilbut.dto.route.request;

import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RouteRecommendationRequest {

    @Valid
    @NotNull
    private PlaceRequest origin;

    @Valid
    @NotNull
    private PlaceRequest destination;

    @NotNull
    private LocalDateTime departureDateTime;

    public RouteCandidateRequest toCandidateRequest() {
        return RouteCandidateRequest.builder()
                .originLatitude(origin.getLatitude())
                .originLongitude(origin.getLongitude())
                .destinationLatitude(destination.getLatitude())
                .destinationLongitude(destination.getLongitude())
                .departureDateTime(departureDateTime)
                .build();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PlaceRequest {

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