package com.gilbeot.gilbut.dto.place.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlaceSearchRequest {

    private String keyword;
    private String lat;
    private String lon;
    private String radiusKm;
    private String page;
    private String size;
}
