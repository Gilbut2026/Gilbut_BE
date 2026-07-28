package com.gilbeot.gilbut.client.tmap.dto.place;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmapPlaceSearchResponse {

    private SearchPoiInfo searchPoiInfo;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchPoiInfo {

        private String totalCount;
        private Pois pois;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pois {

        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<Poi> poi = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Poi {

        private String id;
        private String name;
        private String frontLat;
        private String frontLon;
        private String noorLat;
        private String noorLon;
        private String upperAddrName;
        private String middleAddrName;
        private String lowerAddrName;
        @JsonAlias("detailAddrname")
        private String detailAddrName;
        private NewAddressList newAddressList;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NewAddressList {

        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<NewAddress> newAddress = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NewAddress {

        private String fullAddressRoad;
    }
}
