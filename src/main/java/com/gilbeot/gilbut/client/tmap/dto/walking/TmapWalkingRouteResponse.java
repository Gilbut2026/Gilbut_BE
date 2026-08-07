package com.gilbeot.gilbut.client.tmap.dto.walking;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmapWalkingRouteResponse {

    private List<Feature> features = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {

        private Geometry geometry;
        private Properties properties;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geometry {

        private String type;
        private JsonNode coordinates;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        private Integer totalDistance;
        private Integer totalTime;
        private Integer distance;
        private Integer time;
        private String description;
        private Integer turnType;
        private String pointType;
    }
}
