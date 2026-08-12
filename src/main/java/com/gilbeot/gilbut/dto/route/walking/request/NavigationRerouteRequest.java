package com.gilbeot.gilbut.dto.route.walking.request;

import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NavigationRerouteRequest {

    @Valid
    @NotNull
    private RoutePlaceRequest currentLocation;

    @Valid
    @NotNull
    private RoutePlaceRequest destination;

    private List<WalkingRouteOption> routeOptions;

    public WalkingRouteRequest toWalkingRouteRequest() {
        WalkingRouteRequest request =
                new WalkingRouteRequest();

        request.setOrigin(
                toWalkingPlaceRequest(currentLocation)
        );

        request.setDestination(
                toWalkingPlaceRequest(destination)
        );

        request.setRouteOptions(routeOptions);

        return request;
    }

    private WalkingRouteRequest.RoutePlaceRequest
    toWalkingPlaceRequest(
            RoutePlaceRequest source
    ) {
        if (source == null) {
            return null;
        }

        WalkingRouteRequest.RoutePlaceRequest target =
                new WalkingRouteRequest.RoutePlaceRequest();

        target.setPlaceId(source.getPlaceId());
        target.setName(source.getName());
        target.setAddress(source.getAddress());
        target.setLatitude(source.getLatitude());
        target.setLongitude(source.getLongitude());

        return target;
    }

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
