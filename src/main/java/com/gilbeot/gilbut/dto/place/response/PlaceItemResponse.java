package com.gilbeot.gilbut.dto.place.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlaceItemResponse {

    private String placeId;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
}
