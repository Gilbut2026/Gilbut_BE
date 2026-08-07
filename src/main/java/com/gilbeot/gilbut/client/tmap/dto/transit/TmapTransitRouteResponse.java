package com.gilbeot.gilbut.client.tmap.dto.transit;

import com.fasterxml.jackson.annotation.JsonAlias;
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
public class TmapTransitRouteResponse {

    private MetaData metaData;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaData {

        private Plan plan;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Plan {

        private List<Itinerary> itineraries = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Itinerary {

        private Integer totalTime;
        private Integer totalWalkTime;
        private Integer totalWalkDistance;
        private Integer totalDistance;
        private Integer transferCount;
        private Integer pathType;
        private Fare fare;
        private List<Leg> legs = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fare {

        private Regular regular;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Regular {

        private Integer totalFare;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Leg {

        private Integer distance;
        private Integer sectionTime;
        private String mode;
        private String route;
        private String routeColor;
        private String routeId;
        private Integer type;
        private Integer service;
        private StopPoint start;
        private StopPoint end;
        private List<WalkingStep> steps = new ArrayList<>();
        private PassShape passShape;
        private PassStopList passStopList;

        @JsonAlias({"Lane", "lane"})
        private List<Lane> lanes = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StopPoint {

        private Object name;
        private Object lat;
        private Object lon;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WalkingStep {

        private Integer distance;
        private String streetName;
        private String description;
        private String linestring;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PassShape {

        private String linestring;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PassStopList {

        @JsonAlias({"stations", "stationList"})
        private List<Station> stationList = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Station {

        private Integer index;
        private String stationID;
        private String stationName;
        private Object lon;
        private Object lat;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Lane {

        private String route;
        private String routeColor;
        private String routeId;
        private Integer type;
        private Integer service;
    }
}
