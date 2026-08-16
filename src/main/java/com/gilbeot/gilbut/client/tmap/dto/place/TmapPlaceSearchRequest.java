package com.gilbeot.gilbut.client.tmap.dto.place;

import com.gilbeot.gilbut.dto.place.request.PlaceSearchSort;
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
    private PlaceSearchSort sort;

    public boolean hasCoordinates() {
        return centerLat != null && centerLon != null;
    }
}
