package com.gilbeot.gilbut.service.station;

import com.gilbeot.gilbut.client.gg.GyeonggiElevatorClient;
import com.gilbeot.gilbut.client.tmap.TmapStationClient;
import com.gilbeot.gilbut.domain.station.StationElevator;
import com.gilbeot.gilbut.domain.station.TransitStation;
import com.gilbeot.gilbut.dto.station.request.AlongRouteStationElevatorRequest;
import com.gilbeot.gilbut.dto.station.response.AlongRouteStationElevatorItemResponse;
import com.gilbeot.gilbut.dto.station.response.AlongRouteStationElevatorResponse;
import com.gilbeot.gilbut.dto.station.response.StationElevatorDetailResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StationElevatorAlongRouteService {

    private static final int DEFAULT_RADIUS_METERS = 300;
    private static final int MIN_RADIUS_METERS = 1;
    private static final int MAX_RADIUS_METERS = 1000;
    private static final int MAX_REQUEST_ROUTE_POINT_COUNT = 1000;
    private static final int MAX_TMAP_SEARCH_POINT_COUNT = 30;
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private final TmapStationClient tmapStationClient;
    private final GyeonggiElevatorClient gyeonggiElevatorClient;

    public AlongRouteStationElevatorResponse findAlongRoute(
            AlongRouteStationElevatorRequest request
    ) {
        AlongRouteStationElevatorQuery query =
                createQuery(request);

        Map<String, StationWithRouteDistance> stationByKey =
                new LinkedHashMap<>();

        for (RoutePoint searchPoint : query.searchPoints()) {
            List<TransitStation> stations =
                    tmapStationClient.searchNearbyStations(
                            searchPoint.latitude(),
                            searchPoint.longitude(),
                            query.radiusMeters()
                    );

            for (TransitStation station : stations) {
                int distanceFromRouteM =
                        calculateDistanceFromRouteMeters(
                                station,
                                query.routePoints()
                        );

                if (distanceFromRouteM > query.radiusMeters()) {
                    continue;
                }

                String stationKey =
                        createStationKey(station);

                if (!StringUtils.hasText(stationKey)) {
                    continue;
                }

                stationByKey.merge(
                        stationKey,
                        new StationWithRouteDistance(
                                station,
                                distanceFromRouteM
                        ),
                        (current, candidate) ->
                                current.distanceFromRouteM()
                                        <= candidate.distanceFromRouteM()
                                        ? current
                                        : candidate
                );
            }
        }

        if (stationByKey.isEmpty()) {
            return AlongRouteStationElevatorResponse.builder()
                    .stations(List.of())
                    .build();
        }

        Map<String, List<StationElevator>> elevatorsByStation =
                groupElevatorsByStation(
                        gyeonggiElevatorClient.getElevators()
                );

        List<AlongRouteStationElevatorItemResponse> responseStations =
                stationByKey.values()
                        .stream()
                        .sorted(
                                Comparator
                                        .comparingInt(
                                                StationWithRouteDistance
                                                        ::distanceFromRouteM
                                        )
                                        .thenComparing(item ->
                                                item.station().getName()
                                        )
                        )
                        .map(item ->
                                toResponse(
                                        item,
                                        elevatorsByStation.getOrDefault(
                                                item.station()
                                                        .getNormalizedName(),
                                                List.of()
                                        )
                                )
                        )
                        .toList();

        return AlongRouteStationElevatorResponse.builder()
                .stations(responseStations)
                .build();
    }

    private AlongRouteStationElevatorQuery createQuery(
            AlongRouteStationElevatorRequest request
    ) {
        if (request == null) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        List<RoutePoint> routePoints =
                toRoutePoints(request.getRoutePoints());

        return new AlongRouteStationElevatorQuery(
                routePoints,
                sampleSearchPoints(routePoints),
                parseRadiusMeters(request.getRadiusMeters())
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
            List<AlongRouteStationElevatorRequest.RoutePointRequest>
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
                AlongRouteStationElevatorRequest.RoutePointRequest point
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

    private List<RoutePoint> sampleSearchPoints(
            List<RoutePoint> routePoints
    ) {
        if (routePoints.size() <= MAX_TMAP_SEARCH_POINT_COUNT) {
            return routePoints;
        }

        List<RoutePoint> sampled =
                new ArrayList<>();
        int lastIndex =
                routePoints.size() - 1;

        for (
                int index = 0;
                index < MAX_TMAP_SEARCH_POINT_COUNT;
                index++
        ) {
            int sourceIndex =
                    (int) Math.round(
                            index * (lastIndex / (double)
                                    (MAX_TMAP_SEARCH_POINT_COUNT - 1))
                    );
            RoutePoint point =
                    routePoints.get(sourceIndex);

            if (!isSamePoint(lastPoint(sampled), point)) {
                sampled.add(point);
            }
        }

        return sampled;
    }

    private int calculateDistanceFromRouteMeters(
            TransitStation station,
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
                            station.getLatitude(),
                            station.getLongitude(),
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

    private String createStationKey(
            TransitStation station
    ) {
        if (StringUtils.hasText(station.getStationId())) {
            return "ID:" + station.getStationId();
        }

        if (StringUtils.hasText(station.getNormalizedName())) {
            return "NAME:" + station.getNormalizedName();
        }

        return null;
    }

    private Map<String, List<StationElevator>> groupElevatorsByStation(
            List<StationElevator> elevators
    ) {
        return elevators.stream()
                .filter(elevator ->
                        StringUtils.hasText(
                                elevator.getNormalizedStationName()
                        )
                )
                .collect(
                        Collectors.groupingBy(
                                StationElevator
                                        ::getNormalizedStationName
                        )
                );
    }

    private AlongRouteStationElevatorItemResponse toResponse(
            StationWithRouteDistance item,
            List<StationElevator> elevators
    ) {
        TransitStation station =
                item.station();
        List<StationElevatorDetailResponse> elevatorResponses =
                elevators.stream()
                        .map(this::toElevatorResponse)
                        .toList();

        return AlongRouteStationElevatorItemResponse.builder()
                .stationId(station.getStationId())
                .stationName(station.getName())
                .address(station.getAddress())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .distanceFromRouteM(item.distanceFromRouteM())
                .elevatorCount(elevatorResponses.size())
                .elevators(elevatorResponses)
                .build();
    }

    private StationElevatorDetailResponse toElevatorResponse(
            StationElevator elevator
    ) {
        return StationElevatorDetailResponse.builder()
                .routeName(elevator.getRouteName())
                .operator(elevator.getOperator())
                .exitNumber(elevator.getExitNumber())
                .location(elevator.getLocation())
                .floorRange(elevator.getFloorRange())
                .state(elevator.getState())
                .elevatorNumber(elevator.getElevatorNumber())
                .capacityCount(elevator.getCapacityCount())
                .capacityWeight(elevator.getCapacityWeight())
                .build();
    }

    private record AlongRouteStationElevatorQuery(
            List<RoutePoint> routePoints,
            List<RoutePoint> searchPoints,
            int radiusMeters
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

    private record StationWithRouteDistance(
            TransitStation station,
            int distanceFromRouteM
    ) {
    }
}
