package com.gilbeot.gilbut.dto.route.walking.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SelectedRestStopResponse {

    private String facilityId;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
}
