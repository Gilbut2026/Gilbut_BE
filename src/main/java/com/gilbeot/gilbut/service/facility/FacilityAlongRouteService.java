package com.gilbeot.gilbut.service.facility;

import com.gilbeot.gilbut.domain.facility.Facility;
import com.gilbeot.gilbut.domain.facility.FacilityType;
import com.gilbeot.gilbut.dto.facility.request.AlongRouteFacilityRequest;
import com.gilbeot.gilbut.dto.facility.response.AlongRouteFacilityItemResponse;
import com.gilbeot.gilbut.dto.facility.response.AlongRouteFacilityResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FacilityAlongRouteService {

    private static final int DEFAULT_RADIUS_METERS = 100;
    private static final int MIN_RADIUS_METERS = 1;
    private static final int MAX_RADIUS_METERS = 500;
    private static final int MAX_REQUEST_ROUTE_POINT_COUNT = 1000;
    private static final int MAX_SAMPLE_POINT_COUNT = 300;
    private static final int SAMPLE_INTERVAL_METERS = 30;
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private final FacilityCsvService facilityCsvService;

    public AlongRouteFacilityResponse findAlongRoute(
            AlongRouteFacilityRequest request
    ) {
        AlongRouteFacilityQuery query =
                createQuery(request);

        Map<String, FacilityWithRouteDistance> byFacilityId =
                new LinkedHashMap<>();

        for (
                Facility facility
                        : facilityCsvService.findByTypes(
                        query.types()
                )
        ) {
            int distanceFromRouteM =
                    calculateDistanceFromRouteMeters(
                            facility,
                            query.routePoints()
                    );

            if (distanceFromRouteM > query.radiusMeters()) {
                continue;
            }

            String facilityId =
                    createFacilityId(facility);

            byFacilityId.merge(
                    facilityId,
                    new FacilityWithRouteDistance(
                            facility,
                            distanceFromRouteM
                    ),
                    (current, candidate) ->
                            current.distanceFromRouteM()
                                    <= candidate.distanceFromRouteM()
                                    ? current
                                    : candidate
            );
        }

        List<AlongRouteFacilityItemResponse> items =
                byFacilityId.entrySet()
                        .stream()
                        .map(entry ->
                                toResponse(
                                        entry.getKey(),
                                        entry.getValue()
                                )
                        )
                        .sorted(
                                Comparator
                                        .comparingInt(
                                                AlongRouteFacilityItemResponse
                                                        ::getDistanceFromRouteM
                                        )
                                        .thenComparing(
                                                AlongRouteFacilityItemResponse
                                                        ::getType
                                        )
                                        .thenComparing(
                                                AlongRouteFacilityItemResponse
                                                        ::getName,
                                                Comparator.nullsLast(
                                                        String::compareTo
                                                )
                                        )
                        )
                        .toList();

        return AlongRouteFacilityResponse.builder()
                .items(items)
                .build();
    }

    private AlongRouteFacilityQuery createQuery(
            AlongRouteFacilityRequest request
    ) {
        if (request == null) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        int radiusMeters =
                parseRadiusMeters(request.getRadiusMeters());
        List<RoutePoint> routePoints =
                sampleRoutePoints(
                        toRoutePoints(request.getRoutePoints())
                );
        Set<FacilityType> types =
                parseTypes(request.getTypes());

        return new AlongRouteFacilityQuery(
                routePoints,
                radiusMeters,
                types
        );
    }

    private int parseRadiusMeters(
            Integer value
    ) {
        if (value == null) {
            return DEFAULT_RADIUS_METERS;
        }

        if (value < MIN_RADIUS_METERS
                || value > MAX_RADIUS_METERS) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return value;
    }

    private List<RoutePoint> toRoutePoints(
            List<AlongRouteFacilityRequest.RoutePointRequest>
                    requestPoints
    ) {
        if (requestPoints == null
                || requestPoints.size() < 2
                || requestPoints.size()
                > MAX_REQUEST_ROUTE_POINT_COUNT) {

            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        List<RoutePoint> routePoints =
                new ArrayList<>();

        for (
                AlongRouteFacilityRequest.RoutePointRequest point
                        : requestPoints
        ) {
            if (point == null
                    || point.getLatitude() == null
                    || point.getLongitude() == null
                    || !isValidCoordinate(
                    point.getLatitude(),
                    point.getLongitude()
            )) {

                throw new CustomException(
                        ErrorCode.INVALID_REQUEST
                );
            }

            RoutePoint routePoint =
                    new RoutePoint(
                            point.getLatitude(),
                            point.getLongitude()
                    );

            if (!isSamePoint(lastPoint(routePoints), routePoint)) {
                routePoints.add(routePoint);
            }
        }

        if (routePoints.size() < 2) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return routePoints;
    }

    private List<RoutePoint> sampleRoutePoints(
            List<RoutePoint> routePoints
    ) {
        if (routePoints.size() <= MAX_SAMPLE_POINT_COUNT) {
            return routePoints;
        }

        List<RoutePoint> sampled =
                sampleByDistance(routePoints);

        if (sampled.size() <= MAX_SAMPLE_POINT_COUNT) {
            return sampled;
        }

        return reduceByIndex(sampled);
    }

    private List<RoutePoint> sampleByDistance(
            List<RoutePoint> routePoints
    ) {
        List<RoutePoint> sampled =
                new ArrayList<>();

        RoutePoint first =
                routePoints.get(0);
        RoutePoint last =
                routePoints.get(routePoints.size() - 1);
        RoutePoint lastSampled =
                first;

        sampled.add(first);

        for (int index = 1; index < routePoints.size() - 1; index++) {
            RoutePoint point =
                    routePoints.get(index);

            double distanceMeters =
                    calculateDistanceMeters(
                            lastSampled.latitude(),
                            lastSampled.longitude(),
                            point.latitude(),
                            point.longitude()
                    );

            if (distanceMeters >= SAMPLE_INTERVAL_METERS) {
                sampled.add(point);
                lastSampled = point;
            }
        }

        if (!isSamePoint(lastPoint(sampled), last)) {
            sampled.add(last);
        }

        return sampled;
    }

    private List<RoutePoint> reduceByIndex(
            List<RoutePoint> routePoints
    ) {
        List<RoutePoint> reduced =
                new ArrayList<>();

        int lastIndex =
                routePoints.size() - 1;

        for (int index = 0; index < MAX_SAMPLE_POINT_COUNT; index++) {
            int sourceIndex =
                    (int) Math.round(
                            index * (lastIndex / (double)
                                    (MAX_SAMPLE_POINT_COUNT - 1))
                    );

            RoutePoint point =
                    routePoints.get(sourceIndex);

            if (!isSamePoint(lastPoint(reduced), point)) {
                reduced.add(point);
            }
        }

        return reduced;
    }

    private Set<FacilityType> parseTypes(
            List<FacilityType> types
    ) {
        if (types == null || types.isEmpty()) {
            return EnumSet.allOf(FacilityType.class);
        }

        EnumSet<FacilityType> parsedTypes =
                EnumSet.noneOf(FacilityType.class);

        for (FacilityType type : types) {
            if (type == null) {
                throw new CustomException(
                        ErrorCode.INVALID_REQUEST
                );
            }

            parsedTypes.add(type);
        }

        if (parsedTypes.isEmpty()) {
            return EnumSet.allOf(FacilityType.class);
        }

        return parsedTypes;
    }

    private int calculateDistanceFromRouteMeters(
            Facility facility,
            List<RoutePoint> routePoints
    ) {
        double minDistanceMeters =
                Double.MAX_VALUE;

        for (int index = 0; index < routePoints.size() - 1; index++) {
            RoutePoint start =
                    routePoints.get(index);
            RoutePoint end =
                    routePoints.get(index + 1);

            double distanceMeters =
                    calculatePointToSegmentDistanceMeters(
                            facility.getLatitude(),
                            facility.getLongitude(),
                            start,
                            end
                    );

            if (distanceMeters < minDistanceMeters) {
                minDistanceMeters = distanceMeters;
            }
        }

        return (int) Math.round(minDistanceMeters);
    }

    private double calculatePointToSegmentDistanceMeters(
            double latitude,
            double longitude,
            RoutePoint segmentStart,
            RoutePoint segmentEnd
    ) {
        ProjectedPoint start =
                project(
                        segmentStart.latitude(),
                        segmentStart.longitude(),
                        latitude,
                        longitude
                );
        ProjectedPoint end =
                project(
                        segmentEnd.latitude(),
                        segmentEnd.longitude(),
                        latitude,
                        longitude
                );

        double dx =
                end.x() - start.x();
        double dy =
                end.y() - start.y();

        if (dx == 0 && dy == 0) {
            return Math.hypot(
                    start.x(),
                    start.y()
            );
        }

        double t =
                -(start.x() * dx + start.y() * dy)
                        / (dx * dx + dy * dy);
        double clampedT =
                Math.max(0, Math.min(1, t));
        double closestX =
                start.x() + clampedT * dx;
        double closestY =
                start.y() + clampedT * dy;

        return Math.hypot(closestX, closestY);
    }

    private ProjectedPoint project(
            double latitude,
            double longitude,
            double referenceLatitude,
            double referenceLongitude
    ) {
        double referenceLatRad =
                Math.toRadians(referenceLatitude);
        double x =
                Math.toRadians(longitude - referenceLongitude)
                        * EARTH_RADIUS_METERS
                        * Math.cos(referenceLatRad);
        double y =
                Math.toRadians(latitude - referenceLatitude)
                        * EARTH_RADIUS_METERS;

        return new ProjectedPoint(x, y);
    }

    private double calculateDistanceMeters(
            double fromLatitude,
            double fromLongitude,
            double toLatitude,
            double toLongitude
    ) {
        double fromLatRad = Math.toRadians(fromLatitude);
        double toLatRad = Math.toRadians(toLatitude);
        double deltaLatRad =
                Math.toRadians(toLatitude - fromLatitude);
        double deltaLonRad =
                Math.toRadians(toLongitude - fromLongitude);

        double a =
                Math.sin(deltaLatRad / 2)
                        * Math.sin(deltaLatRad / 2)
                        + Math.cos(fromLatRad)
                        * Math.cos(toLatRad)
                        * Math.sin(deltaLonRad / 2)
                        * Math.sin(deltaLonRad / 2);
        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        return EARTH_RADIUS_METERS * c;
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

    private RoutePoint lastPoint(
            List<RoutePoint> routePoints
    ) {
        if (routePoints.isEmpty()) {
            return null;
        }

        return routePoints.get(routePoints.size() - 1);
    }

    private boolean isSamePoint(
            RoutePoint first,
            RoutePoint second
    ) {
        if (first == null || second == null) {
            return false;
        }

        return Double.compare(
                first.latitude(),
                second.latitude()
        ) == 0
                && Double.compare(
                first.longitude(),
                second.longitude()
        ) == 0;
    }

    private String createFacilityId(
            Facility facility
    ) {
        return facility.getType().name()
                + "-"
                + facility.getSourceId();
    }

    private AlongRouteFacilityItemResponse toResponse(
            String facilityId,
            FacilityWithRouteDistance item
    ) {
        Facility facility =
                item.facility();

        return AlongRouteFacilityItemResponse.builder()
                .type(facility.getType())
                .facilityId(facilityId)
                .name(facility.getName())
                .category(facility.getCategory())
                .subcategory(facility.getSubcategory())
                .address(facility.getAddress())
                .latitude(facility.getLatitude())
                .longitude(facility.getLongitude())
                .distanceFromRouteM(
                        item.distanceFromRouteM()
                )
                .phone(facility.getPhone())
                .operatingHours(facility.getOperatingHours())
                .status(facility.getStatus())
                .sourceDate(facility.getSourceDate())
                .build();
    }

    private record AlongRouteFacilityQuery(
            List<RoutePoint> routePoints,
            int radiusMeters,
            Set<FacilityType> types
    ) {
    }

    private record RoutePoint(
            double latitude,
            double longitude
    ) {
    }

    private record ProjectedPoint(
            double x,
            double y
    ) {
    }

    private record FacilityWithRouteDistance(
            Facility facility,
            int distanceFromRouteM
    ) {
    }
}
