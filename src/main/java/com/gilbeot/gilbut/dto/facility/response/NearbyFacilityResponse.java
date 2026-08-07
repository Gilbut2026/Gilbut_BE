package com.gilbeot.gilbut.dto.facility.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NearbyFacilityResponse {

    private List<FacilityItemResponse> items;
}
