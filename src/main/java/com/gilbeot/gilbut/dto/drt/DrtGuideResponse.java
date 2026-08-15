package com.gilbeot.gilbut.dto.drt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DrtGuideResponse {

    private Boolean show;
    private String serviceName;
    private DrtServiceArea serviceArea;
    private String serviceAreaName;
    private String contactNumber;
    private DrtAvailability availability;
    private String message;
}