package com.gilbeot.gilbut.service.station;

import com.gilbeot.gilbut.client.gg.GyeonggiElevatorClient;
import com.gilbeot.gilbut.client.tmap.TmapStationClient;
import com.gilbeot.gilbut.domain.station.StationElevator;
import com.gilbeot.gilbut.domain.station.TransitStation;
import com.gilbeot.gilbut.dto.station.request.NearbyStationElevatorRequest;
import com.gilbeot.gilbut.dto.station.response.NearbyStationElevatorResponse;
import com.gilbeot.gilbut.dto.station.response.StationElevatorDetailResponse;
import com.gilbeot.gilbut.dto.station.response.StationElevatorItemResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StationElevatorNearbyService {

    private static final int DEFAULT_RADIUS_METERS = 3000;
    private static final int MIN_RADIUS_METERS = 1;
    private static final int MAX_RADIUS_METERS = 20000;

    private final TmapStationClient tmapStationClient;
    private final GyeonggiElevatorClient gyeonggiElevatorClient;

    public NearbyStationElevatorResponse findNearby(
            NearbyStationElevatorRequest request
    ) {
        NearbyStationElevatorQuery query = createQuery(request);
        List<TransitStation> stations =
                deduplicateStations(
                        tmapStationClient.searchNearbyStations(
                                query.latitude(),
                                query.longitude(),
                                query.radiusMeters()
                        )
                );
        Map<String, List<StationElevator>> elevatorsByStation =
                groupElevatorsByStation(
                        gyeonggiElevatorClient.getElevators()
                );

        List<StationElevatorItemResponse> responseStations =
                stations.stream()
                        .map(station ->
                                toResponse(
                                        station,
                                        elevatorsByStation
                                                .getOrDefault(
                                                        station
                                                                .getNormalizedName(),
                                                        List.of()
                                                )
                                )
                        )
                        .toList();

        return NearbyStationElevatorResponse.builder()
                .stations(responseStations)
                .build();
    }

    private NearbyStationElevatorQuery createQuery(
            NearbyStationElevatorRequest request
    ) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        return new NearbyStationElevatorQuery(
                parseLatitude(request.getLat()),
                parseLongitude(request.getLng()),
                parseRadiusMeters(request.getRadiusMeters())
        );
    }

    private double parseLatitude(
            String value
    ) {
        double latitude = parseDouble(value);

        if (latitude < -90 || latitude > 90) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        return latitude;
    }

    private double parseLongitude(
            String value
    ) {
        double longitude = parseDouble(value);

        if (longitude < -180 || longitude > 180) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        return longitude;
    }

    private int parseRadiusMeters(
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return DEFAULT_RADIUS_METERS;
        }

        int radiusMeters = parseInteger(value);

        if (radiusMeters < MIN_RADIUS_METERS
                || radiusMeters > MAX_RADIUS_METERS) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        return radiusMeters;
    }

    private double parseDouble(
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        try {
            return Double.parseDouble(value.trim());

        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private int parseInteger(
            String value
    ) {
        try {
            return Integer.parseInt(value.trim());

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private List<TransitStation> deduplicateStations(
            List<TransitStation> stations
    ) {
        Map<String, TransitStation> stationByName =
                new LinkedHashMap<>();

        stations.stream()
                .sorted(
                        Comparator.comparingInt(
                                TransitStation::getDistanceMeters
                        )
                )
                .forEach(station ->
                        stationByName.putIfAbsent(
                                station.getNormalizedName(),
                                station
                        )
                );

        return stationByName.values()
                .stream()
                .sorted(
                        Comparator.comparingInt(
                                TransitStation::getDistanceMeters
                        )
                )
                .toList();
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

    private StationElevatorItemResponse toResponse(
            TransitStation station,
            List<StationElevator> elevators
    ) {
        List<StationElevatorDetailResponse> elevatorResponses =
                elevators.stream()
                        .map(this::toElevatorResponse)
                        .toList();

        return StationElevatorItemResponse.builder()
                .stationId(station.getStationId())
                .stationName(station.getName())
                .address(station.getAddress())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .distanceMeters(station.getDistanceMeters())
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

    private record NearbyStationElevatorQuery(
            double latitude,
            double longitude,
            int radiusMeters
    ) {
    }
}
