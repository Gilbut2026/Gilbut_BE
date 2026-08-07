package com.gilbeot.gilbut.client.tmap.dto.walking;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TmapWalkingRouteRequest {

    private Double startX;
    private Double startY;
    private Double endX;
    private Double endY;
    private String reqCoordType;
    private String resCoordType;
    private String startName;
    private String endName;
    private String searchOption;
}