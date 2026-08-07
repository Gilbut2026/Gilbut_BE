package com.gilbeot.gilbut.dto.facility.response;

import com.gilbeot.gilbut.domain.facility.FacilityType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FacilityItemResponse {

    private FacilityType type;
    private String facilityId;
    private String name;
    private String category;
    private String subcategory;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer distanceMeters;
    private String phone;
    private String operatingHours;
    private String status;
    private String sourceDate;
}
