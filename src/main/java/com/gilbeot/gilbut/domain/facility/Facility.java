package com.gilbeot.gilbut.domain.facility;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Facility {

    private FacilityType type;
    private String sourceId;
    private String name;
    private String category;
    private String subcategory;
    private String address;
    private double latitude;
    private double longitude;
    private String phone;
    private String operatingHours;
    private String status;
    private String sourceDate;
}
