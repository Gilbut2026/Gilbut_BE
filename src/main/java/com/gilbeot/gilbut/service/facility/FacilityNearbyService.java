package com.gilbeot.gilbut.service.facility;

import com.gilbeot.gilbut.domain.facility.Facility;
import com.gilbeot.gilbut.domain.facility.FacilityType;
import com.gilbeot.gilbut.dto.facility.request.NearbyFacilityRequest;
import com.gilbeot.gilbut.dto.facility.response.FacilityItemResponse;
import com.gilbeot.gilbut.dto.facility.response.NearbyFacilityResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FacilityNearbyService {

    private static final int DEFAULT_RADIUS_METERS = 250;
    private static final int MIN_RADIUS_METERS = 1;
    private static final int MAX_RADIUS_METERS = 5000;
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private final FacilityCsvService facilityCsvService;

    public NearbyFacilityResponse findNearby(
            NearbyFacilityRequest request
    ) {
        NearbyFacilityQuery query = createQuery(request);

        List<FacilityItemResponse> items =
                facilityCsvService.findByTypes(query.types())
                        .stream()
                        .map(facility ->
                                toFacilityWithDistance(
                                        facility,
                                        query.latitude(),
                                        query.longitude()
                                )
                        )
                        .filter(item ->
                                item.distanceMeters()
                                        <= query.radiusMeters()
                        )
                        .sorted(
                                Comparator
                                        .comparingInt(
                                                FacilityWithDistance
                                                        ::distanceMeters
                                        )
                                        .thenComparing(item ->
                                                item.facility().getType()
                                        )
                                        .thenComparing(item ->
                                                item.facility().getName()
                                        )
                        )
                        .map(FacilityNearbyService::toResponse)
                        .toList();

        return NearbyFacilityResponse.builder()
                .items(items)
                .build();
    }

    private NearbyFacilityQuery createQuery(
            NearbyFacilityRequest request
    ) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        double latitude = parseLatitude(request.getLat());
        double longitude = parseLongitude(request.getLng());
        int radiusMeters =
                parseRadiusMeters(request.getRadiusMeters());
        Set<FacilityType> types = parseTypes(request.getTypes());

        return new NearbyFacilityQuery(
                latitude,
                longitude,
                radiusMeters,
                types
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

    private Set<FacilityType> parseTypes(
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return EnumSet.allOf(FacilityType.class);
        }

        EnumSet<FacilityType> types =
                EnumSet.noneOf(FacilityType.class);

        Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(type ->
                        type.toUpperCase(Locale.ROOT)
                )
                .forEach(type -> {
                    try {
                        types.add(FacilityType.valueOf(type));

                    } catch (IllegalArgumentException e) {
                        throw new CustomException(
                                ErrorCode.INVALID_REQUEST
                        );
                    }
                });

        if (types.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        return types;
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

    private FacilityWithDistance toFacilityWithDistance(
            Facility facility,
            double latitude,
            double longitude
    ) {
        int distanceMeters =
                (int) Math.round(
                        calculateDistanceMeters(
                                latitude,
                                longitude,
                                facility.getLatitude(),
                                facility.getLongitude()
                        )
                );

        return new FacilityWithDistance(
                facility,
                distanceMeters
        );
    }

    private static FacilityItemResponse toResponse(
            FacilityWithDistance item
    ) {
        Facility facility = item.facility();

        return FacilityItemResponse.builder()
                .type(facility.getType())
                .facilityId(
                        facility.getType().name()
                                + "-"
                                + facility.getSourceId()
                )
                .name(facility.getName())
                .category(facility.getCategory())
                .subcategory(facility.getSubcategory())
                .address(facility.getAddress())
                .latitude(facility.getLatitude())
                .longitude(facility.getLongitude())
                .distanceMeters(item.distanceMeters())
                .phone(facility.getPhone())
                .operatingHours(facility.getOperatingHours())
                .status(facility.getStatus())
                .sourceDate(facility.getSourceDate())
                .build();
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

    private record NearbyFacilityQuery(
            double latitude,
            double longitude,
            int radiusMeters,
            Set<FacilityType> types
    ) {
    }

    private record FacilityWithDistance(
            Facility facility,
            int distanceMeters
    ) {
    }
}
