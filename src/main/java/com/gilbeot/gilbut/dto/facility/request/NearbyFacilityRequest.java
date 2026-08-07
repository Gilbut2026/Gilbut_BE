package com.gilbeot.gilbut.dto.facility.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NearbyFacilityRequest {

    private String lat;
    private String lng;
    private String radiusMeters;
    private String types;
}
