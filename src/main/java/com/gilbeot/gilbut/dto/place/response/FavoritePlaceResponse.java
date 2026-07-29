package com.gilbeot.gilbut.dto.place.response;

import com.gilbeot.gilbut.domain.favorite.FavoritePlace;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FavoritePlaceResponse {

    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;

    public static FavoritePlaceResponse from(FavoritePlace favoritePlace) {
        return FavoritePlaceResponse.builder()
                .id(favoritePlace.getId())
                .name(favoritePlace.getName())
                .address(favoritePlace.getAddress())
                .latitude(favoritePlace.getLatitude())
                .longitude(favoritePlace.getLongitude())
                .build();
    }
}
