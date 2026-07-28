package com.gilbeot.gilbut.dto.place.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PlaceSearchResponse {

    private List<PlaceItemResponse> places;
    private PaginationResponse pagination;

    @Getter
    @Builder
    public static class PaginationResponse {

        private int page;
        private int size;
        private int totalCount;
        private boolean hasNext;
    }
}
