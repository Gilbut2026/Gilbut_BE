package com.gilbeot.gilbut.client.tmap;

import com.gilbeot.gilbut.client.tmap.dto.transit.TmapTransitRouteRequest;
import com.gilbeot.gilbut.client.tmap.dto.transit.TmapTransitRouteResponse;
import com.gilbeot.gilbut.dto.route.TransitRouteFailureCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.global.exception.TransitRouteSearchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Locale;

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
                throw new TransitRouteSearchException(
                        TransitRouteFailureCode.PROVIDER_ERROR
                );
            }

            return body;

        } catch (CustomException e) {
            throw e;

        } catch (RestClientResponseException e) {
            TransitRouteFailureCode failureCode =
                    classifyFailure(e);

            log.error(
                    "TMAP 대중교통 경로 조회 실패: status={}, reason={}",
                    e.getStatusCode()
                            .value(),
                    failureCode,
                    e
            );

            throw new TransitRouteSearchException(
                    failureCode
            );

        } catch (Exception e) {
            log.error(
                    "TMAP 대중교통 경로 조회 중 오류 발생",
                    e
            );

            throw new TransitRouteSearchException(
                    TransitRouteFailureCode.PROVIDER_ERROR
            );
        }
    }

    private TransitRouteFailureCode classifyFailure(
            RestClientResponseException exception
    ) {
        int statusCode =
                exception.getStatusCode()
                        .value();

        String responseBody =
                exception.getResponseBodyAsString();

        String normalizedBody =
                responseBody == null
                        ? ""
                        : responseBody.toLowerCase(
                                Locale.ROOT
                        );

        if (statusCode == 429
                || containsAny(
                normalizedBody,
                "quota",
                "rate limit",
                "too many",
                "limit exceeded",
                "한도",
                "쿼터"
        )) {
            return TransitRouteFailureCode.QUOTA_EXCEEDED;
        }

        if (statusCode == 401
                || statusCode == 403
                || containsAny(
                normalizedBody,
                "unauthorized",
                "forbidden",
                "permission",
                "appkey",
                "api key",
                "apikey",
                "권한",
                "인증",
                "키"
        )) {
            return TransitRouteFailureCode.KEY_OR_PERMISSION;
        }

        if (containsAny(
                normalizedBody,
                "no route",
                "route not found",
                "no path",
                "no itinerary",
                "no result",
                "결과가 없습니다",
                "노선이 없습니다",
                "노선을 찾을 수 없습니다",
                "경로를 찾을 수 없습니다",
                "경로 없음"
        )) {
            return TransitRouteFailureCode.NO_ROUTE;
        }

        return TransitRouteFailureCode.PROVIDER_ERROR;
    }

    private boolean containsAny(
            String text,
            String... tokens
    ) {
        if (text == null || tokens == null) {
            return false;
        }

        for (String token : tokens) {
            if (token != null
                    && text.contains(token)) {
                return true;
            }
        }

        return false;
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
