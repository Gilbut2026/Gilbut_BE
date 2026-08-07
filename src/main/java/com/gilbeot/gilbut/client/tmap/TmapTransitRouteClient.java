package com.gilbeot.gilbut.client.tmap;

import com.gilbeot.gilbut.client.tmap.dto.transit.TmapTransitRouteRequest;
import com.gilbeot.gilbut.client.tmap.dto.transit.TmapTransitRouteResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class TmapTransitRouteClient {

    private static final String TMAP_TRANSIT_ROUTE_URL =
            "https://apis.openapi.sk.com/transit/routes";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tmap.app-key}")
    private String tmapAppKey;

    public TmapTransitRouteResponse search(
            TmapTransitRouteRequest request
    ) {
        try {
            ResponseEntity<TmapTransitRouteResponse> response =
                    restTemplate.exchange(
                            TMAP_TRANSIT_ROUTE_URL,
                            HttpMethod.POST,
                            buildRequest(request),
                            TmapTransitRouteResponse.class
                    );

            TmapTransitRouteResponse body = response.getBody();

            if (body == null) {
                throw new CustomException(
                        ErrorCode.ROUTE_SEARCH_FAILED
                );
            }

            return body;

        } catch (CustomException e) {
            throw e;

        } catch (Exception e) {
            log.error(
                    "TMAP 대중교통 경로 조회 중 오류 발생",
                    e
            );

            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }
    }

    private HttpEntity<TmapTransitRouteRequest> buildRequest(
            TmapTransitRouteRequest request
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("appKey", tmapAppKey);

        return new HttpEntity<>(
                request,
                headers
        );
    }
}