package com.gilbeot.gilbut.dto.place.response;

import com.gilbeot.gilbut.domain.home.HomePlace;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomePlaceResponse {

    private String address;
    private Double latitude;
    private Double longitude;

    public static HomePlaceResponse from(HomePlace homePlace) {
        return HomePlaceResponse.builder()
                .address(homePlace.getAddress())
                .latitude(homePlace.getLatitude())
                .longitude(homePlace.getLongitude())
                .build();
    }
}