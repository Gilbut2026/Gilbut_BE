package com.gilbeot.gilbut.client.tmap;

import com.gilbeot.gilbut.client.tmap.dto.geocoding.TmapReverseGeocodingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TmapReverseGeocodingClient {

    private static final String TMAP_REVERSE_GEOCODING_URL =
            "https://apis.openapi.sk.com/tmap/geo/reversegeocoding";

    private final RestTemplate restTemplate;

    @Value("${tmap.app-key}")
    private String tmapAppKey;

    public TmapReverseGeocodingClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${tmap.reverse-geocoding.connect-timeout-ms:1000}")
            long connectTimeoutMs,
            @Value("${tmap.reverse-geocoding.read-timeout-ms:1500}")
            long readTimeoutMs
    ) {
        this.restTemplate =
                restTemplateBuilder
                        .setConnectTimeout(
                                Duration.ofMillis(connectTimeoutMs)
                        )
                        .setReadTimeout(
                                Duration.ofMillis(readTimeoutMs)
                        )
                        .build();
    }

    public Optional<TmapReverseGeocodingResponse.AddressInfo> search(
            double latitude,
            double longitude
    ) {
        try {
            ResponseEntity<TmapReverseGeocodingResponse> response =
                    restTemplate.exchange(
                            buildUri(latitude, longitude),
                            HttpMethod.GET,
                            buildRequest(),
                            TmapReverseGeocodingResponse.class
                    );

            TmapReverseGeocodingResponse body = response.getBody();

            if (body == null || body.getAddressInfo() == null) {
                return Optional.empty();
            }

            return Optional.of(body.getAddressInfo());

        } catch (Exception e) {
            log.warn(
                    "TMAP 리버스 지오코딩 조회 실패. latitude={}, longitude={}",
                    latitude,
                    longitude,
                    e
            );

            return Optional.empty();
        }
    }

    private URI buildUri(
            double latitude,
            double longitude
    ) {
        return UriComponentsBuilder
                .fromHttpUrl(TMAP_REVERSE_GEOCODING_URL)
                .queryParam("version", "1")
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("coordType", "WGS84GEO")
                .queryParam("addressType", "A10")
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();
    }

    private HttpEntity<Void> buildRequest() {
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(
                List.of(MediaType.APPLICATION_JSON)
        );
        headers.set(
                "appKey",
                tmapAppKey
        );

        return new HttpEntity<>(headers);
    }
}