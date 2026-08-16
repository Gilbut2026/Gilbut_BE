package com.gilbeot.gilbut.service.place;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gilbeot.gilbut.client.tmap.dto.place.TmapPlaceSearchRequest;
import com.gilbeot.gilbut.client.tmap.dto.place.TmapPlaceSearchResponse;
import com.gilbeot.gilbut.dto.place.request.PlaceSearchRequest;
import com.gilbeot.gilbut.dto.place.request.PlaceSearchSort;
import com.gilbeot.gilbut.dto.place.response.PlaceItemResponse;
import com.gilbeot.gilbut.dto.place.response.PlaceSearchResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private static final String TMAP_POI_URL =
            "https://apis.openapi.sk.com/tmap/pois";
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;
    private static final int DEFAULT_RADIUS_KM = 5;
    private static final int MIN_RADIUS_KM = 1;
    private static final int MAX_RADIUS_KM = 33;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${tmap.app-key}")
    private String tmapAppKey;

    public PlaceSearchResponse search(PlaceSearchRequest request) {
        TmapPlaceSearchRequest tmapRequest =
                createTmapRequest(request);

        try {
            URI uri = buildTmapUri(tmapRequest);
            ResponseEntity<String> response =
                    restTemplate.exchange(
                            uri,
                            HttpMethod.GET,
                            buildTmapRequest(),
                            String.class
                    );

            TmapPlaceSearchResponse tmapResponse =
                    objectMapper.readValue(
                            response.getBody(),
                            TmapPlaceSearchResponse.class
                    );

            return toPlaceSearchResponse(tmapResponse, tmapRequest);

        } catch (CustomException e) {
            throw e;

        } catch (Exception e) {
            log.error("장소 검색 처리 중 오류 발생", e);
            throw new CustomException(ErrorCode.PLACE_SEARCH_FAILED);
        }
    }

    private TmapPlaceSearchRequest createTmapRequest(
            PlaceSearchRequest request
    ) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        String normalizedKeyword = normalizeKeyword(request.getKeyword());
        boolean hasLat = StringUtils.hasText(request.getLat());
        boolean hasLon = StringUtils.hasText(request.getLon());

        if (hasLat != hasLon) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        boolean hasCoordinates = hasLat;
        boolean hasRadius = StringUtils.hasText(request.getRadiusKm());
        PlaceSearchSort sort =
                PlaceSearchSort.from(
                        request.getSort(),
                        hasCoordinates
                );

        if (!hasCoordinates && hasRadius) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (sort == PlaceSearchSort.DISTANCE
                && !hasCoordinates) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Double centerLat = null;
        Double centerLon = null;
        Integer radius = null;

        if (hasCoordinates) {
            centerLat = parseLatitude(request.getLat());
            centerLon = parseLongitude(request.getLon());
            radius = hasRadius
                    ? parseRadiusKm(request.getRadiusKm())
                    : DEFAULT_RADIUS_KM;
        }

        return TmapPlaceSearchRequest.builder()
                .keyword(normalizedKeyword)
                .centerLat(centerLat)
                .centerLon(centerLon)
                .radiusKm(radius)
                .page(parsePage(request.getPage()))
                .size(parseSize(request.getSize()))
                .sort(sort)
                .build();
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        String normalizedKeyword = keyword.trim();

        if (normalizedKeyword.length() > 100) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        return normalizedKeyword;
    }

    private Double parseLatitude(String value) {
        double latitude = parseDouble(value);

        if (latitude < -90 || latitude > 90) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        return latitude;
    }

    private Double parseLongitude(String value) {
        double longitude = parseDouble(value);

        if (longitude < -180 || longitude > 180) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        return longitude;
    }

    private Integer parseRadiusKm(String value) {
        int radius = parseInteger(value);

        if (radius < MIN_RADIUS_KM || radius > MAX_RADIUS_KM) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        return radius;
    }

    private Integer parsePage(String value) {
        if (!StringUtils.hasText(value)) {
            return DEFAULT_PAGE;
        }

        int page = parseInteger(value);

        if (page < 1) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        return page;
    }

    private Integer parseSize(String value) {
        if (!StringUtils.hasText(value)) {
            return DEFAULT_SIZE;
        }

        int size = parseInteger(value);

        if (size < 1 || size > MAX_SIZE) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        return size;
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private URI buildTmapUri(TmapPlaceSearchRequest request) {
        UriComponentsBuilder builder =
                UriComponentsBuilder
                        .fromHttpUrl(TMAP_POI_URL)
                        .queryParam("version", "1")
                        .queryParam("searchKeyword", request.getKeyword())
                        .queryParam("searchType", "all")
                        .queryParam("page", request.getPage())
                        .queryParam("count", request.getSize())
                        .queryParam("reqCoordType", "WGS84GEO")
                        .queryParam("resCoordType", "WGS84GEO")
                        .queryParam("multiPoint", "N")
                        .queryParam("poiGroupYn", "N")
                        .queryParam(
                                "searchtypCd",
                                request.getSort()
                                        .getTmapCode()
                        );

        if (request.hasCoordinates()) {
            builder
                    .queryParam("centerLat", request.getCenterLat())
                    .queryParam("centerLon", request.getCenterLon())
                    .queryParam("radius", request.getRadiusKm());
        }

        return builder
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();
    }

    private HttpEntity<Void> buildTmapRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("appKey", tmapAppKey);

        return new HttpEntity<>(headers);
    }

    private PlaceSearchResponse toPlaceSearchResponse(
            TmapPlaceSearchResponse tmapResponse,
            TmapPlaceSearchRequest request
    ) {
        TmapPlaceSearchResponse.SearchPoiInfo searchPoiInfo =
                tmapResponse.getSearchPoiInfo();
        int totalCount =
                parseIntegerOrDefault(
                        searchPoiInfo == null
                                ? null
                                : searchPoiInfo.getTotalCount(),
                        0
                );
        List<PlaceItemResponse> places =
                extractPlaces(searchPoiInfo);

        PlaceSearchResponse.PaginationResponse pagination =
                PlaceSearchResponse.PaginationResponse.builder()
                        .page(request.getPage())
                        .size(request.getSize())
                        .totalCount(totalCount)
                        .hasNext(
                                (long) request.getPage() * request.getSize()
                                        < totalCount
                        )
                        .build();

        return PlaceSearchResponse.builder()
                .places(places)
                .pagination(pagination)
                .build();
    }

    private List<PlaceItemResponse> extractPlaces(
            TmapPlaceSearchResponse.SearchPoiInfo searchPoiInfo
    ) {
        List<PlaceItemResponse> places = new ArrayList<>();

        if (searchPoiInfo == null
                || searchPoiInfo.getPois() == null
                || searchPoiInfo.getPois().getPoi() == null) {

            return places;
        }

        searchPoiInfo.getPois()
                .getPoi()
                .forEach(poi ->
                        toPlaceItemResponse(poi).ifPresent(places::add)
                );

        return places;
    }

    private Optional<PlaceItemResponse> toPlaceItemResponse(
            TmapPlaceSearchResponse.Poi poi
    ) {
        String name = textOrNull(poi.getName());
        Coordinate coordinate = resolveCoordinate(poi);

        if (!StringUtils.hasText(name) || coordinate == null) {
            return Optional.empty();
        }

        return Optional.of(
                PlaceItemResponse.builder()
                        .placeId(textOrNull(poi.getId()))
                        .name(name)
                        .address(resolveAddress(poi))
                        .latitude(coordinate.latitude())
                        .longitude(coordinate.longitude())
                        .build()
        );
    }

    private Coordinate resolveCoordinate(TmapPlaceSearchResponse.Poi poi) {
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
            double latitude = Double.parseDouble(latitudeValue.trim());
            double longitude = Double.parseDouble(longitudeValue.trim());

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

    private String resolveAddress(TmapPlaceSearchResponse.Poi poi) {
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
                        addressParts
                                .stream()
                                .filter(StringUtils::hasText)
                                .toList()
                )
                .trim();

        return StringUtils.hasText(address) ? address : null;
    }

    private String resolveRoadAddress(TmapPlaceSearchResponse.Poi poi) {
        if (poi.getNewAddressList() == null
                || poi.getNewAddressList().getNewAddress() == null) {

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

    private String textOrNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private int parseIntegerOrDefault(String value, int defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());

        } catch (Exception e) {
            return defaultValue;
        }
    }

    private record Coordinate(
            double latitude,
            double longitude
    ) {
    }
}
