package com.gilbeot.gilbut.service.route;

import com.fasterxml.jackson.databind.JsonNode;
import com.gilbeot.gilbut.client.tmap.TmapWalkingRouteClient;
import com.gilbeot.gilbut.client.tmap.dto.walking.TmapWalkingRouteRequest;
import com.gilbeot.gilbut.client.tmap.dto.walking.TmapWalkingRouteResponse;
import com.gilbeot.gilbut.dto.route.walking.request.WalkingRouteRequest;
import com.gilbeot.gilbut.dto.route.walking.response.RoutePointResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteSummaryResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingStepResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalkingRouteService {

    private static final String DEFAULT_ORIGIN_NAME = "출발지";
    private static final String DEFAULT_DESTINATION_NAME = "목적지";

    private final TmapWalkingRouteClient tmapWalkingRouteClient;

    public WalkingRouteResponse search(
            WalkingRouteRequest request
    ) {
        validateRequest(request);

        TmapWalkingRouteResponse tmapResponse =
                tmapWalkingRouteClient.search(
                        toTmapRequest(request)
                );

        return toResponse(tmapResponse);
    }

    private TmapWalkingRouteRequest toTmapRequest(
            WalkingRouteRequest request
    ) {
        WalkingRouteRequest.RoutePlaceRequest origin =
                request.getOrigin();
        WalkingRouteRequest.RoutePlaceRequest destination =
                request.getDestination();

        return TmapWalkingRouteRequest.builder()
                .startX(origin.getLongitude())
                .startY(origin.getLatitude())
                .endX(destination.getLongitude())
                .endY(destination.getLatitude())
                .reqCoordType("WGS84GEO")
                .resCoordType("WGS84GEO")
                .startName(
                        textOrDefault(
                                origin.getName(),
                                DEFAULT_ORIGIN_NAME
                        )
                )
                .endName(
                        textOrDefault(
                                destination.getName(),
                                DEFAULT_DESTINATION_NAME
                        )
                )
                .searchOption("0")
                .build();
    }

    private WalkingRouteResponse toResponse(
            TmapWalkingRouteResponse response
    ) {
        if (response == null || response.getFeatures() == null) {
            throw new CustomException(ErrorCode.ROUTE_SEARCH_FAILED);
        }

        TmapWalkingRouteResponse.Properties summary =
                extractSummary(response);
        RouteParseResult parseResult =
                parseRoute(response.getFeatures());

        if (parseResult.routePoints().isEmpty()) {
            throw new CustomException(ErrorCode.ROUTE_SEARCH_FAILED);
        }

        return WalkingRouteResponse.builder()
                .routeId(generateRouteId())
                .summary(
                        WalkingRouteSummaryResponse.builder()
                                .totalDistanceM(
                                        summary.getTotalDistance()
                                )
                                .totalTimeSec(summary.getTotalTime())
                                .build()
                )
                .routePoints(parseResult.routePoints())
                .steps(parseResult.steps())
                .build();
    }

    private TmapWalkingRouteResponse.Properties extractSummary(
            TmapWalkingRouteResponse response
    ) {
        return response.getFeatures()
                .stream()
                .map(TmapWalkingRouteResponse.Feature::getProperties)
                .filter(properties -> properties != null)
                .filter(properties ->
                        properties.getTotalDistance() != null
                                && properties.getTotalTime() != null
                )
                .findFirst()
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.ROUTE_SEARCH_FAILED
                        )
                );
    }

    private RouteParseResult parseRoute(
            List<TmapWalkingRouteResponse.Feature> features
    ) {
        List<RoutePointResponse> routePoints =
                new ArrayList<>();
        List<WalkingStepResponse> steps =
                new ArrayList<>();
        PendingInstruction pendingInstruction = null;
        int stepIndex = 1;

        for (TmapWalkingRouteResponse.Feature feature : features) {
            if (feature == null || feature.getGeometry() == null) {
                continue;
            }

            String geometryType = feature.getGeometry().getType();

            if ("Point".equalsIgnoreCase(geometryType)) {
                pendingInstruction =
                        toPendingInstruction(feature);
                continue;
            }

            if (!"LineString".equalsIgnoreCase(geometryType)) {
                continue;
            }

            List<RoutePointResponse> linePoints =
                    extractLinePoints(
                            feature.getGeometry().getCoordinates()
                    );

            appendRoutePoints(routePoints, linePoints);

            if (!linePoints.isEmpty()) {
                steps.add(
                        toStep(
                                stepIndex++,
                                pendingInstruction,
                                feature.getProperties(),
                                linePoints
                        )
                );
            }
        }

        return new RouteParseResult(routePoints, steps);
    }

    private PendingInstruction toPendingInstruction(
            TmapWalkingRouteResponse.Feature feature
    ) {
        TmapWalkingRouteResponse.Properties properties =
                feature.getProperties();

        if (properties == null) {
            return null;
        }

        return new PendingInstruction(
                properties.getDescription(),
                properties.getTurnType(),
                properties.getPointType()
        );
    }

    private WalkingStepResponse toStep(
            int stepIndex,
            PendingInstruction pendingInstruction,
            TmapWalkingRouteResponse.Properties lineProperties,
            List<RoutePointResponse> points
    ) {
        String instruction =
                pendingInstruction == null
                        ? null
                        : pendingInstruction.instruction();

        if (!StringUtils.hasText(instruction)
                && lineProperties != null) {
            instruction = lineProperties.getDescription();
        }

        return WalkingStepResponse.builder()
                .stepIndex(stepIndex)
                .instruction(textOrNull(instruction))
                .distanceM(
                        lineProperties == null
                                ? null
                                : lineProperties.getDistance()
                )
                .durationSec(
                        lineProperties == null
                                ? null
                                : lineProperties.getTime()
                )
                .turnType(
                        pendingInstruction == null
                                ? null
                                : pendingInstruction.turnType()
                )
                .pointType(
                        pendingInstruction == null
                                ? null
                                : pendingInstruction.pointType()
                )
                .points(points)
                .build();
    }

    private List<RoutePointResponse> extractLinePoints(
            JsonNode coordinates
    ) {
        if (coordinates == null || !coordinates.isArray()) {
            return List.of();
        }

        List<RoutePointResponse> points = new ArrayList<>();

        coordinates.forEach(coordinate ->
                toPoint(coordinate).ifPresent(points::add)
        );

        return points;
    }

    private Optional<RoutePointResponse> toPoint(
            JsonNode coordinate
    ) {
        JsonNode pair = coordinate;

        if (coordinate != null
                && coordinate.isObject()
                && coordinate.get("value") != null) {
            pair = coordinate.get("value");
        }

        if (pair == null
                || !pair.isArray()
                || pair.size() < 2
                || !pair.get(0).isNumber()
                || !pair.get(1).isNumber()) {

            return Optional.empty();
        }

        double longitude = pair.get(0).asDouble();
        double latitude = pair.get(1).asDouble();

        if (!isValidCoordinate(latitude, longitude)) {
            return Optional.empty();
        }

        return Optional.of(
                RoutePointResponse.builder()
                        .latitude(latitude)
                        .longitude(longitude)
                        .build()
        );
    }

    private void appendRoutePoints(
            List<RoutePointResponse> routePoints,
            List<RoutePointResponse> linePoints
    ) {
        for (RoutePointResponse point : linePoints) {
            if (!isSamePoint(lastPoint(routePoints), point)) {
                routePoints.add(point);
            }
        }
    }

    private RoutePointResponse lastPoint(
            List<RoutePointResponse> points
    ) {
        if (points.isEmpty()) {
            return null;
        }

        return points.get(points.size() - 1);
    }

    private boolean isSamePoint(
            RoutePointResponse first,
            RoutePointResponse second
    ) {
        if (first == null || second == null) {
            return false;
        }

        return Double.compare(
                first.getLatitude(),
                second.getLatitude()
        ) == 0
                && Double.compare(
                first.getLongitude(),
                second.getLongitude()
        ) == 0;
    }

    private void validateRequest(
            WalkingRouteRequest request
    ) {
        if (request == null
                || request.getOrigin() == null
                || request.getDestination() == null
                || request.getOrigin().getLatitude() == null
                || request.getOrigin().getLongitude() == null
                || request.getDestination().getLatitude() == null
                || request.getDestination().getLongitude() == null) {

            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        validateCoordinate(
                request.getOrigin().getLatitude(),
                request.getOrigin().getLongitude()
        );
        validateCoordinate(
                request.getDestination().getLatitude(),
                request.getDestination().getLongitude()
        );
    }

    private void validateCoordinate(
            double latitude,
            double longitude
    ) {
        if (!isValidCoordinate(latitude, longitude)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private boolean isValidCoordinate(
            double latitude,
            double longitude
    ) {
        return latitude >= -90
                && latitude <= 90
                && longitude >= -180
                && longitude <= 180
                && !(latitude == 0 && longitude == 0);
    }

    private String textOrDefault(
            String value,
            String defaultValue
    ) {
        return StringUtils.hasText(value)
                ? value.trim()
                : defaultValue;
    }

    private String textOrNull(
            String value
    ) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }

    private String generateRouteId() {
        return "walking-" + UUID.randomUUID();
    }

    private record PendingInstruction(
            String instruction,
            Integer turnType,
            String pointType
    ) {
    }

    private record RouteParseResult(
            List<RoutePointResponse> routePoints,
            List<WalkingStepResponse> steps
    ) {
    }
}
