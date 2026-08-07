package com.gilbeot.gilbut.client.tmap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gilbeot.gilbut.client.tmap.dto.place.TmapPlaceSearchResponse;
import com.gilbeot.gilbut.domain.station.TransitStation;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.service.station.StationNameNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TmapStationClient {

    private static final String TMAP_POI_URL =
            "https://apis.openapi.sk.com/tmap/pois";
    private static final String STATION_KEYWORD = "지하철역";
    private static final int PAGE_SIZE = 50;
    private static final int MAX_PAGES = 5;
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${tmap.app-key}")
    private String tmapAppKey;

    public List<TransitStation> searchNearbyStations(
            double latitude,
            double longitude,
            int radiusMeters
    ) {
        try {
            return fetchStations(
                    latitude,
                    longitude,
                    radiusMeters
            );

        } catch (CustomException e) {
            throw e;

        } catch (RestClientResponseException e) {
            if (isEmptySearchResponse(e)) {
                return List.of();
            }

            log.error(
                    "TMAP 주변 역 검색 HTTP 오류 발생. status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );

            throw new CustomException(
                    ErrorCode.STATION_SEARCH_FAILED
            );

        } catch (Exception e) {
            log.error("TMAP 주변 역 검색 처리 중 오류 발생", e);
            throw new CustomException(
                    ErrorCode.STATION_SEARCH_FAILED
            );
        }
    }

    private List<TransitStation> fetchStations(
            double latitude,
            double longitude,
            int radiusMeters
    ) throws Exception {
        List<TransitStation> stations = new ArrayList<>();
        int radiusKm =
                Math.max(
                        1,
                        (int) Math.ceil(radiusMeters / 1000.0)
                );

        for (int page = 1; page <= MAX_PAGES; page++) {
            URI uri = buildUri(
                    latitude,
                    longitude,
                    radiusKm,
                    page
            );
            ResponseEntity<String> response =
                    restTemplate.exchange(
                            uri,
                            HttpMethod.GET,
                            buildRequest(),
                            String.class
                    );
            String responseBody =
                    response.getBody();

            if (!StringUtils.hasText(responseBody)) {
                break;
            }

            JsonNode payload =
                    objectMapper.readTree(responseBody);

            if (hasNoPois(payload)) {
                break;
            }

            TmapPlaceSearchResponse tmapResponse =
                    objectMapper.treeToValue(
                            payload,
                            TmapPlaceSearchResponse.class
                    );

            stations.addAll(
                    extractStations(
                            tmapResponse,
                            latitude,
                            longitude,
                            radiusMeters
                    )
            );

            int totalCount = totalCount(tmapResponse);

            if (totalCount <= page * PAGE_SIZE) {
                break;
            }
        }

        return stations;
    }

    private URI buildUri(
            double latitude,
            double longitude,
            int radiusKm,
            int page
    ) {
        return UriComponentsBuilder
                .fromHttpUrl(TMAP_POI_URL)
                .queryParam("version", "1")
                .queryParam("searchKeyword", STATION_KEYWORD)
                .queryParam("searchType", "all")
                .queryParam("searchtypCd", "R")
                .queryParam("centerLat", latitude)
                .queryParam("centerLon", longitude)
                .queryParam("radius", radiusKm)
                .queryParam("page", page)
                .queryParam("count", PAGE_SIZE)
                .queryParam("reqCoordType", "WGS84GEO")
                .queryParam("resCoordType", "WGS84GEO")
                .queryParam("multiPoint", "N")
                .queryParam("poiGroupYn", "N")
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();
    }

    private HttpEntity<Void> buildRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("appKey", tmapAppKey);

        return new HttpEntity<>(headers);
    }

    private List<TransitStation> extractStations(
            TmapPlaceSearchResponse response,
            double latitude,
            double longitude,
            int radiusMeters
    ) {
        List<TransitStation> stations = new ArrayList<>();

        if (response == null
                || response.getSearchPoiInfo() == null
                || response.getSearchPoiInfo().getPois() == null
                || response.getSearchPoiInfo()
                .getPois()
                .getPoi() == null) {

            return stations;
        }

        response.getSearchPoiInfo()
                .getPois()
                .getPoi()
                .forEach(poi ->
                        toStation(
                                poi,
                                latitude,
                                longitude,
                                radiusMeters
                        ).ifPresent(stations::add)
                );

        return stations;
    }

    private Optional<TransitStation> toStation(
            TmapPlaceSearchResponse.Poi poi,
            double latitude,
            double longitude,
            int radiusMeters
    ) {
        Coordinate coordinate = resolveCoordinate(poi);
        String name = textOrNull(poi.getName());
        String address = resolveAddress(poi);

        if (coordinate == null
                || !StringUtils.hasText(name)
                || !isSuwonAddress(address)) {

            return Optional.empty();
        }

        String normalizedName =
                StationNameNormalizer.normalize(name);

        if (!StringUtils.hasText(normalizedName)) {
            return Optional.empty();
        }

        int distanceMeters =
                (int) Math.round(
                        calculateDistanceMeters(
                                latitude,
                                longitude,
                                coordinate.latitude(),
                                coordinate.longitude()
                        )
                );

        if (distanceMeters > radiusMeters) {
            return Optional.empty();
        }

        return Optional.of(
                TransitStation.builder()
                        .stationId(textOrNull(poi.getId()))
                        .name(name)
                        .normalizedName(normalizedName)
                        .address(address)
                        .latitude(coordinate.latitude())
                        .longitude(coordinate.longitude())
                        .distanceMeters(distanceMeters)
                        .build()
        );
    }

    private boolean isSuwonAddress(
            String address
    ) {
        return StringUtils.hasText(address)
                && address.contains("수원시");
    }

    private Coordinate resolveCoordinate(
            TmapPlaceSearchResponse.Poi poi
    ) {
        Coordinate frontCoordinate =
                createCoordinate(
                        textOrNull(poi.getFrontLat()),
                        textOrNull(poi.getFrontLon())
                );

        if (frontCoordinate != null) {
            return frontCoordinate;
        }

        return createCoordinate(
                textOrNull(poi.getNoorLat()),
                textOrNull(poi.getNoorLon())
        );
    }

    private Coordinate createCoordinate(
            String latitudeValue,
            String longitudeValue
    ) {
        if (!StringUtils.hasText(latitudeValue)
                || !StringUtils.hasText(longitudeValue)) {
            return null;
        }

        try {
            double latitude =
                    Double.parseDouble(latitudeValue.trim());
            double longitude =
                    Double.parseDouble(longitudeValue.trim());

            if (latitude < -90
                    || latitude > 90
                    || longitude < -180
                    || longitude > 180
                    || (latitude == 0 && longitude == 0)) {
                return null;
            }

            return new Coordinate(latitude, longitude);

        } catch (Exception e) {
            return null;
        }
    }

    private String resolveAddress(
            TmapPlaceSearchResponse.Poi poi
    ) {
        String roadAddress = resolveRoadAddress(poi);

        if (StringUtils.hasText(roadAddress)) {
            return roadAddress;
        }

        List<String> addressParts = List.of(
                nullToBlank(textOrNull(poi.getUpperAddrName())),
                nullToBlank(textOrNull(poi.getMiddleAddrName())),
                nullToBlank(textOrNull(poi.getLowerAddrName())),
                nullToBlank(textOrNull(poi.getDetailAddrName()))
        );

        String address = String.join(
                        " ",
                        addressParts.stream()
                                .filter(StringUtils::hasText)
                                .toList()
                )
                .trim();

        return StringUtils.hasText(address)
                ? address
                : null;
    }

    private String resolveRoadAddress(
            TmapPlaceSearchResponse.Poi poi
    ) {
        if (poi.getNewAddressList() == null
                || poi.getNewAddressList()
                .getNewAddress() == null) {

            return null;
        }

        for (TmapPlaceSearchResponse.NewAddress address :
                poi.getNewAddressList().getNewAddress()) {

            String fullAddressRoad =
                    textOrNull(address.getFullAddressRoad());

            if (StringUtils.hasText(fullAddressRoad)) {
                return fullAddressRoad;
            }
        }

        return null;
    }

    private int totalCount(
            TmapPlaceSearchResponse response
    ) {
        if (response == null
                || response.getSearchPoiInfo() == null
                || !StringUtils.hasText(
                response.getSearchPoiInfo().getTotalCount()
        )) {

            return 0;
        }

        try {
            return Integer.parseInt(
                    response.getSearchPoiInfo()
                            .getTotalCount()
                            .trim()
            );

        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean hasNoPois(
            JsonNode payload
    ) {
        JsonNode searchPoiInfo =
                payload == null
                        ? null
                        : payload.get("searchPoiInfo");

        if (searchPoiInfo == null || searchPoiInfo.isNull()) {
            return true;
        }

        int totalCount =
                safeInt(
                        searchPoiInfo.path("totalCount").asText(),
                        0
                );

        if (totalCount == 0) {
            return true;
        }

        JsonNode pois =
                searchPoiInfo.get("pois");

        if (pois == null || pois.isNull()) {
            return true;
        }

        if (pois.isTextual()) {
            return !StringUtils.hasText(pois.asText());
        }

        JsonNode poi =
                pois.get("poi");

        if (poi == null || poi.isNull()) {
            return true;
        }

        return poi.isTextual()
                && !StringUtils.hasText(poi.asText());
    }

    private boolean isEmptySearchResponse(
            RestClientResponseException e
    ) {
        int statusCode =
                e.getStatusCode().value();

        if (statusCode != 400 && statusCode != 404) {
            return false;
        }

        String body =
                e.getResponseBodyAsString();

        return !StringUtils.hasText(body)
                || body.contains("NO_RESULT")
                || body.contains("no result")
                || body.contains("not found")
                || body.contains("검색 결과")
                || body.contains("결과가 없습니다")
                || body.contains("결과 없음");
    }

    private int safeInt(
            String value,
            int defaultValue
    ) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());

        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String textOrNull(
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private String nullToBlank(
            String value
    ) {
        return value == null ? "" : value;
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

    private record Coordinate(
            double latitude,
            double longitude
    ) {
    }
}
