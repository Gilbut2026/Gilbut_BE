package com.gilbeot.gilbut.client.tmap;

import com.gilbeot.gilbut.client.tmap.dto.walking.TmapWalkingRouteRequest;
import com.gilbeot.gilbut.client.tmap.dto.walking.TmapWalkingRouteResponse;
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
public class TmapWalkingRouteClient {

    private static final String TMAP_WALKING_ROUTE_URL =
            "https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&format=json";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tmap.app-key}")
    private String tmapAppKey;

    public TmapWalkingRouteResponse search(
            TmapWalkingRouteRequest request
    ) {
        try {
            ResponseEntity<TmapWalkingRouteResponse> response =
                    restTemplate.exchange(
                            TMAP_WALKING_ROUTE_URL,
                            HttpMethod.POST,
                            buildRequest(request),
                            TmapWalkingRouteResponse.class
                    );

            TmapWalkingRouteResponse body = response.getBody();

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
                    "TMAP 보행 경로 조회 중 오류 발생",
                    e
            );

            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }
    }

    private HttpEntity<TmapWalkingRouteRequest> buildRequest(
            TmapWalkingRouteRequest request
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