package com.gilbeot.gilbut.client.tmap.dto.place;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TmapPlaceSearchRequest {

    private String keyword;
    private Double centerLat;
    private Double centerLon;
    private Integer radiusKm;
    private int page;
    private int size;

    public boolean hasCoordinates() {
        return centerLat != null && centerLon != null;
    }
}
