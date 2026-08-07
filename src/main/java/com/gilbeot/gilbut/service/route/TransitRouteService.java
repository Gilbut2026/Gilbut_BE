package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.client.tmap.TmapTransitRouteClient;
import com.gilbeot.gilbut.client.tmap.dto.transit.TmapTransitRouteRequest;
import com.gilbeot.gilbut.client.tmap.dto.transit.TmapTransitRouteResponse;
import com.gilbeot.gilbut.dto.route.transit.request.TransitRouteRequest;
import com.gilbeot.gilbut.dto.route.transit.response.TransitLegResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteItemResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRoutePointResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteSummaryResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitStopResponse;
import com.gilbeot.gilbut.dto.route.transit.response.TransitWalkingStepResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TransitRouteService {

    private static final int DEFAULT_COUNT = 5;

    private static final DateTimeFormatter SEARCH_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private static final Pattern COORDINATE_PATTERN =
            Pattern.compile(
                    "(-?\\d+(?:\\.\\d+)?),\\s*(-?\\d+(?:\\.\\d+)?)"
            );

    private final TmapTransitRouteClient tmapTransitRouteClient;

    public TransitRouteResponse search(
            TransitRouteRequest request
    ) {
        validateRequest(request);

        TmapTransitRouteResponse tmapResponse =
                tmapTransitRouteClient.search(
                        toTmapRequest(request)
                );

        List<TmapTransitRouteResponse.Itinerary> itineraries =
                extractItineraries(tmapResponse);

        if (itineraries.isEmpty()) {
            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }

        return TransitRouteResponse.builder()
                .routes(toRoutes(itineraries))
                .build();
    }

    private TmapTransitRouteRequest toTmapRequest(
            TransitRouteRequest request
    ) {
        TransitRouteRequest.RoutePlaceRequest origin =
                request.getOrigin();
        TransitRouteRequest.RoutePlaceRequest destination =
                request.getDestination();

        return TmapTransitRouteRequest.builder()
                .startX(String.valueOf(origin.getLongitude()))
                .startY(String.valueOf(origin.getLatitude()))
                .endX(String.valueOf(destination.getLongitude()))
                .endY(String.valueOf(destination.getLatitude()))
                .count(DEFAULT_COUNT)
                .lang(0)
                .format("json")
                .searchDttm(
                        request.getDepartureDateTime()
                                .format(
                                        SEARCH_DATETIME_FORMATTER
                                )
                )
                .build();
    }

    private List<TmapTransitRouteResponse.Itinerary>
    extractItineraries(
            TmapTransitRouteResponse response
    ) {
        if (response == null
                || response.getMetaData() == null
                || response.getMetaData().getPlan() == null
                || response.getMetaData()
                .getPlan()
                .getItineraries() == null) {

            return List.of();
        }

        return response.getMetaData()
                .getPlan()
                .getItineraries();
    }

    private List<TransitRouteItemResponse> toRoutes(
            List<TmapTransitRouteResponse.Itinerary> itineraries
    ) {
        List<TransitRouteItemResponse> routes =
                new ArrayList<>();

        for (
                int index = 0;
                index < itineraries.size();
                index++
        ) {
            int rank = index + 1;
            TmapTransitRouteResponse.Itinerary itinerary =
                    itineraries.get(index);
            List<TransitLegResponse> legs =
                    toLegs(itinerary.getLegs());

            routes.add(
                    TransitRouteItemResponse.builder()
                            .routeId(generateRouteId())
                            .providerRank(rank)
                            .summary(
                                    TransitRouteSummaryResponse.builder()
                                            .totalTimeSec(
                                                    itinerary.getTotalTime()
                                            )
                                            .totalWalkTimeSec(
                                                    itinerary.getTotalWalkTime()
                                            )
                                            .totalWalkDistanceM(
                                                    itinerary.getTotalWalkDistance()
                                            )
                                            .totalDistanceM(
                                                    itinerary.getTotalDistance()
                                            )
                                            .transferCount(
                                                    itinerary.getTransferCount()
                                            )
                                            .fareKrw(
                                                    extractFareKrw(
                                                            itinerary
                                                    )
                                            )
                                            .pathType(
                                                    itinerary.getPathType()
                                            )
                                            .build()
                            )
                            .routePoints(
                                    flattenRoutePoints(legs)
                            )
                            .legs(legs)
                            .build()
            );
        }

        return routes;
    }

    private List<TransitLegResponse> toLegs(
            List<TmapTransitRouteResponse.Leg> tmapLegs
    ) {
        if (tmapLegs == null || tmapLegs.isEmpty()) {
            return List.of();
        }

        List<TransitLegResponse> legs =
                new ArrayList<>();

        for (
                int index = 0;
                index < tmapLegs.size();
                index++
        ) {
            TmapTransitRouteResponse.Leg leg =
                    tmapLegs.get(index);

            if (leg == null) {
                continue;
            }

            List<TransitRoutePointResponse> routePoints =
                    extractLegRoutePoints(leg);
            List<TransitStopResponse> stops =
                    toStops(leg);

            legs.add(
                    TransitLegResponse.builder()
                            .legIndex(index + 1)
                            .mode(normalizeMode(leg.getMode()))
                            .routeName(extractRouteName(leg))
                            .routeColor(extractRouteColor(leg))
                            .providerRouteId(
                                    extractProviderRouteId(leg)
                            )
                            .vehicleType(extractVehicleType(leg))
                            .serviceAvailable(
                                    toServiceAvailable(
                                            extractService(leg)
                                    )
                            )
                            .startName(
                                    toText(
                                            leg.getStart(),
                                            true
                                    )
                            )
                            .startLatitude(
                                    toLatitude(leg.getStart())
                            )
                            .startLongitude(
                                    toLongitude(leg.getStart())
                            )
                            .endName(
                                    toText(
                                            leg.getEnd(),
                                            false
                                    )
                            )
                            .endLatitude(toLatitude(leg.getEnd()))
                            .endLongitude(
                                    toLongitude(leg.getEnd())
                            )
                            .distanceM(leg.getDistance())
                            .durationSec(leg.getSectionTime())
                            .stationCount(
                                    stops.isEmpty()
                                            ? null
                                            : stops.size()
                            )
                            .routePoints(routePoints)
                            .stops(stops)
                            .steps(toWalkingSteps(leg))
                            .build()
            );
        }

        return legs;
    }

    private List<TransitRoutePointResponse> flattenRoutePoints(
            List<TransitLegResponse> legs
    ) {
        if (legs == null || legs.isEmpty()) {
            return List.of();
        }

        List<TransitRoutePointResponse> routePoints =
                new ArrayList<>();

        for (TransitLegResponse leg : legs) {
            appendRoutePoints(
                    routePoints,
                    leg.getRoutePoints()
            );
        }

        return routePoints;
    }

    private List<TransitRoutePointResponse> extractLegRoutePoints(
            TmapTransitRouteResponse.Leg leg
    ) {
        List<TransitRoutePointResponse> points =
                new ArrayList<>();

        if ("WALK".equalsIgnoreCase(leg.getMode())) {
            if (leg.getSteps() != null) {
                for (
                        TmapTransitRouteResponse.WalkingStep step
                                : leg.getSteps()
                ) {
                    if (step == null) {
                        continue;
                    }

                    appendRoutePoints(
                            points,
                            parseLineString(
                                    step.getLinestring()
                            )
                    );
                }
            }
        } else if (leg.getPassShape() != null) {
            appendRoutePoints(
                    points,
                    parseLineString(
                            leg.getPassShape()
                                    .getLinestring()
                    )
            );
        }

        if (points.isEmpty()) {
            appendPoint(
                    points,
                    toPoint(leg.getStart())
            );
            appendPoint(
                    points,
                    toPoint(leg.getEnd())
            );
        }

        return points;
    }

    private List<TransitWalkingStepResponse> toWalkingSteps(
            TmapTransitRouteResponse.Leg leg
    ) {
        if (leg.getSteps() == null || leg.getSteps().isEmpty()) {
            return List.of();
        }

        List<TransitWalkingStepResponse> steps =
                new ArrayList<>();

        for (
                int index = 0;
                index < leg.getSteps().size();
                index++
        ) {
            TmapTransitRouteResponse.WalkingStep step =
                    leg.getSteps().get(index);

            if (step == null) {
                continue;
            }

            steps.add(
                    TransitWalkingStepResponse.builder()
                            .stepIndex(index + 1)
                            .instruction(
                                    textOrNull(
                                            step.getDescription()
                                    )
                            )
                            .streetName(
                                    textOrNull(
                                            step.getStreetName()
                                    )
                            )
                            .distanceM(step.getDistance())
                            .points(
                                    parseLineString(
                                            step.getLinestring()
                                    )
                            )
                            .build()
            );
        }

        return steps;
    }

    private List<TransitStopResponse> toStops(
            TmapTransitRouteResponse.Leg leg
    ) {
        if (leg.getPassStopList() == null
                || leg.getPassStopList().getStationList() == null
                || leg.getPassStopList()
                .getStationList()
                .isEmpty()) {

            return List.of();
        }

        List<TransitStopResponse> stops =
                new ArrayList<>();

        for (
                TmapTransitRouteResponse.Station station
                        : leg.getPassStopList()
                        .getStationList()
        ) {
            if (station == null) {
                continue;
            }

            stops.add(
                    TransitStopResponse.builder()
                            .stopIndex(station.getIndex())
                            .stationId(
                                    textOrNull(
                                            station.getStationID()
                                    )
                            )
                            .name(
                                    textOrNull(
                                            station.getStationName()
                                    )
                            )
                            .latitude(
                                    toDouble(
                                            station.getLat()
                                    )
                            )
                            .longitude(
                                    toDouble(
                                            station.getLon()
                                    )
                            )
                            .build()
            );
        }

        return stops;
    }

    private List<TransitRoutePointResponse> parseLineString(
            String linestring
    ) {
        if (!StringUtils.hasText(linestring)) {
            return List.of();
        }

        List<TransitRoutePointResponse> points =
                new ArrayList<>();
        Matcher matcher =
                COORDINATE_PATTERN.matcher(linestring);

        while (matcher.find()) {
            Double longitude = parseDouble(
                    matcher.group(1)
            );
            Double latitude = parseDouble(
                    matcher.group(2)
            );

            if (latitude == null || longitude == null) {
                continue;
            }

            appendPoint(
                    points,
                    toPoint(latitude, longitude)
            );
        }

        return points;
    }

    private void appendRoutePoints(
            List<TransitRoutePointResponse> routePoints,
            List<TransitRoutePointResponse> linePoints
    ) {
        if (linePoints == null || linePoints.isEmpty()) {
            return;
        }

        for (TransitRoutePointResponse point : linePoints) {
            appendPoint(routePoints, point);
        }
    }

    private void appendPoint(
            List<TransitRoutePointResponse> points,
            TransitRoutePointResponse point
    ) {
        if (point == null
                || isSamePoint(lastPoint(points), point)) {
            return;
        }

        points.add(point);
    }

    private TransitRoutePointResponse lastPoint(
            List<TransitRoutePointResponse> points
    ) {
        if (points.isEmpty()) {
            return null;
        }

        return points.get(points.size() - 1);
    }

    private boolean isSamePoint(
            TransitRoutePointResponse first,
            TransitRoutePointResponse second
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

    private TransitRoutePointResponse toPoint(
            TmapTransitRouteResponse.StopPoint point
    ) {
        if (point == null) {
            return null;
        }

        Double latitude =
                toDouble(point.getLat());
        Double longitude =
                toDouble(point.getLon());

        if (latitude == null || longitude == null) {
            return null;
        }

        return toPoint(latitude, longitude);
    }

    private TransitRoutePointResponse toPoint(
            double latitude,
            double longitude
    ) {
        if (!isValidCoordinate(latitude, longitude)) {
            return null;
        }

        return TransitRoutePointResponse.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    private String normalizeMode(
            String mode
    ) {
        if (!StringUtils.hasText(mode)) {
            return "UNKNOWN";
        }

        String normalized =
                mode.trim().toUpperCase();

        return switch (normalized) {
            case "WALK",
                    "BUS",
                    "SUBWAY",
                    "EXPRESSBUS",
                    "TRAIN",
                    "AIRPLANE",
                    "FERRY" -> normalized;
            default -> "UNKNOWN";
        };
    }

    private String extractRouteName(
            TmapTransitRouteResponse.Leg leg
    ) {
        if (StringUtils.hasText(leg.getRoute())) {
            return leg.getRoute().trim();
        }

        return firstLane(leg) == null
                ? null
                : textOrNull(firstLane(leg).getRoute());
    }

    private String extractRouteColor(
            TmapTransitRouteResponse.Leg leg
    ) {
        if (StringUtils.hasText(leg.getRouteColor())) {
            return leg.getRouteColor().trim();
        }

        return firstLane(leg) == null
                ? null
                : textOrNull(firstLane(leg).getRouteColor());
    }

    private String extractProviderRouteId(
            TmapTransitRouteResponse.Leg leg
    ) {
        if (StringUtils.hasText(leg.getRouteId())) {
            return leg.getRouteId().trim();
        }

        return firstLane(leg) == null
                ? null
                : textOrNull(firstLane(leg).getRouteId());
    }

    private Integer extractVehicleType(
            TmapTransitRouteResponse.Leg leg
    ) {
        if (leg.getType() != null) {
            return leg.getType();
        }

        return firstLane(leg) == null
                ? null
                : firstLane(leg).getType();
    }

    private Integer extractService(
            TmapTransitRouteResponse.Leg leg
    ) {
        if (leg.getService() != null) {
            return leg.getService();
        }

        return firstLane(leg) == null
                ? null
                : firstLane(leg).getService();
    }

    private TmapTransitRouteResponse.Lane firstLane(
            TmapTransitRouteResponse.Leg leg
    ) {
        if (leg.getLanes() == null
                || leg.getLanes().isEmpty()) {
            return null;
        }

        return leg.getLanes().get(0);
    }

    private Boolean toServiceAvailable(
            Integer service
    ) {
        if (service == null) {
            return null;
        }

        return service == 1;
    }

    private Integer extractFareKrw(
            TmapTransitRouteResponse.Itinerary itinerary
    ) {
        if (itinerary.getFare() == null
                || itinerary.getFare().getRegular() == null) {
            return null;
        }

        return itinerary.getFare()
                .getRegular()
                .getTotalFare();
    }

    private String toText(
            TmapTransitRouteResponse.StopPoint point,
            boolean start
    ) {
        if (point == null) {
            return start ? "출발지" : "도착지";
        }

        String name =
                textOrNull(point.getName());

        if (name != null) {
            return name;
        }

        return start ? "출발지" : "도착지";
    }

    private Double toLatitude(
            TmapTransitRouteResponse.StopPoint point
    ) {
        if (point == null) {
            return null;
        }

        return toDouble(point.getLat());
    }

    private Double toLongitude(
            TmapTransitRouteResponse.StopPoint point
    ) {
        if (point == null) {
            return null;
        }

        return toDouble(point.getLon());
    }

    private String textOrNull(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        String text = String.valueOf(value).trim();

        return StringUtils.hasText(text)
                ? text
                : null;
    }

    private Double toDouble(
            Object value
    ) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }

        return parseDouble(
                textOrNull(value)
        );
    }

    private Double parseDouble(
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void validateRequest(
            TransitRouteRequest request
    ) {
        if (request == null
                || request.getOrigin() == null
                || request.getDestination() == null
                || request.getDepartureDateTime() == null
                || request.getOrigin().getLatitude() == null
                || request.getOrigin().getLongitude() == null
                || request.getDestination().getLatitude() == null
                || request.getDestination().getLongitude() == null) {

            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
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
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
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

    private String generateRouteId() {
        return "transit-" + UUID.randomUUID();
    }
}
