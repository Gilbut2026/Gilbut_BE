package com.gilbeot.gilbut.dto.route;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RouteCandidateRequest {

    private Double originLatitude;
    private Double originLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private LocalDateTime departureDateTime;
}