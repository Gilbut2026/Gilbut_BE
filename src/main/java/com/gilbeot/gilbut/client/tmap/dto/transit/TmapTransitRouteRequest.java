package com.gilbeot.gilbut.client.tmap.dto.transit;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TmapTransitRouteRequest {

    private String startX;
    private String startY;
    private String endX;
    private String endY;
    private Integer count;
    private Integer lang;
    private String format;
    private String searchDttm;
}