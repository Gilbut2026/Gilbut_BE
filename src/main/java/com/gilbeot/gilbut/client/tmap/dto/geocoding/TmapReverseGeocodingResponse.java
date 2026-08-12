package com.gilbeot.gilbut.client.tmap.dto.geocoding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmapReverseGeocodingResponse {

    private AddressInfo addressInfo;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressInfo {

        private String fullAddress;

        @JsonProperty("city_do")
        private String cityDo;

        @JsonProperty("gu_gun")
        private String guGun;

        private String adminDong;
        private String legalDong;
    }
}