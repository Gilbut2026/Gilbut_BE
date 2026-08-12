package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.client.tmap.dto.walking.TmapWalkingRouteResponse;
import com.gilbeot.gilbut.dto.route.RouteAccessibilitySignals;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class WalkingAccessibilitySignalExtractor {

    private static final int STAIR_FACILITY_TYPE = 17;
    private static final int OVERPASS_FACILITY_TYPE = 12;
    private static final int UNDERPASS_FACILITY_TYPE = 14;

    private static final int OVERPASS_TURN_TYPE = 125;
    private static final int UNDERPASS_TURN_TYPE = 126;
    private static final int STAIR_TURN_TYPE = 127;
    private static final int STAIR_RAMP_TURN_TYPE = 129;

    public RouteAccessibilitySignals extract(
            TmapWalkingRouteResponse response
    ) {
        if (response == null || response.getFeatures() == null) {
            return RouteAccessibilitySignals.unknown();
        }

        Map<ObstacleType, Integer> facilityCounts =
                initializeCounts();
        Map<ObstacleType, Integer> turnCounts =
                initializeCounts();

        ObstacleType previousLineObstacle = null;

        for (TmapWalkingRouteResponse.Feature feature
                : response.getFeatures()) {

            if (feature == null
                    || feature.getGeometry() == null
                    || feature.getProperties() == null) {
                continue;
            }

            String geometryType =
                    feature.getGeometry().getType();

            TmapWalkingRouteResponse.Properties properties =
                    feature.getProperties();

            if ("LineString".equalsIgnoreCase(geometryType)) {
                ObstacleType currentObstacle =
                        obstacleFromFacilityType(
                                properties.getFacilityType()
                        );

                if (currentObstacle != null
                        && currentObstacle != previousLineObstacle) {
                    facilityCounts.compute(
                            currentObstacle,
                            (key, count) -> count + 1
                    );
                }

                previousLineObstacle = currentObstacle;
                continue;
            }

            if ("Point".equalsIgnoreCase(geometryType)) {
                ObstacleType obstacle =
                        obstacleFromTurnType(
                                properties.getTurnType()
                        );

                if (obstacle != null) {
                    turnCounts.compute(
                            obstacle,
                            (key, count) -> count + 1
                    );
                }
            }
        }

        return RouteAccessibilitySignals.known(
                resolveCount(
                        ObstacleType.STAIR,
                        facilityCounts,
                        turnCounts
                ),
                resolveCount(
                        ObstacleType.OVERPASS,
                        facilityCounts,
                        turnCounts
                ),
                resolveCount(
                        ObstacleType.UNDERPASS,
                        facilityCounts,
                        turnCounts
                )
        );
    }

    private Map<ObstacleType, Integer> initializeCounts() {
        Map<ObstacleType, Integer> counts =
                new EnumMap<>(ObstacleType.class);

        for (ObstacleType type : ObstacleType.values()) {
            counts.put(type, 0);
        }

        return counts;
    }

    private int resolveCount(
            ObstacleType type,
            Map<ObstacleType, Integer> facilityCounts,
            Map<ObstacleType, Integer> turnCounts
    ) {
        int facilityCount = facilityCounts.get(type);

        if (facilityCount > 0) {
            return facilityCount;
        }

        return turnCounts.get(type);
    }

    private ObstacleType obstacleFromFacilityType(
            Integer facilityType
    ) {
        if (facilityType == null) {
            return null;
        }

        return switch (facilityType) {
            case STAIR_FACILITY_TYPE -> ObstacleType.STAIR;
            case OVERPASS_FACILITY_TYPE -> ObstacleType.OVERPASS;
            case UNDERPASS_FACILITY_TYPE -> ObstacleType.UNDERPASS;
            default -> null;
        };
    }

    private ObstacleType obstacleFromTurnType(
            Integer turnType
    ) {
        if (turnType == null) {
            return null;
        }

        return switch (turnType) {
            case STAIR_TURN_TYPE,
                    STAIR_RAMP_TURN_TYPE -> ObstacleType.STAIR;
            case OVERPASS_TURN_TYPE -> ObstacleType.OVERPASS;
            case UNDERPASS_TURN_TYPE -> ObstacleType.UNDERPASS;
            default -> null;
        };
    }

    private enum ObstacleType {
        STAIR,
        OVERPASS,
        UNDERPASS
    }
}