package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.client.tmap.TmapWalkingRouteClient;
import com.gilbeot.gilbut.client.tmap.dto.walking.TmapWalkingRouteRequest;
import com.gilbeot.gilbut.client.tmap.dto.walking.TmapWalkingRouteResponse;
import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
import com.gilbeot.gilbut.dto.route.RouteMetrics;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalkingRouteCandidateService {

    private final TmapWalkingRouteClient tmapWalkingRouteClient;

    public RouteCandidate createCandidate(
            RouteCandidateRequest request
    ) {
        validateRequest(request);

        TmapWalkingRouteResponse response =
                tmapWalkingRouteClient.search(
                        toTmapRequest(request)
                );

        TmapWalkingRouteResponse.Properties properties =
                extractSummary(response);

        return RouteCandidate.builder()
                .routeId("walking-1")
                .routeType(RouteType.WALKING)
                .providerRank(1)
                .metrics(
                        RouteMetrics.builder()
                                .totalTimeSec(
                                        properties.getTotalTime()
                                )
                                .totalWalkTimeSec(
                                        properties.getTotalTime()
                                )
                                .totalWalkDistanceM(
                                        properties.getTotalDistance()
                                )
                                .transferCount(0)
                                .build()
                )
                .build();
    }

    private TmapWalkingRouteRequest toTmapRequest(
            RouteCandidateRequest request
    ) {
        return TmapWalkingRouteRequest.builder()
                .startX(request.getOriginLongitude())
                .startY(request.getOriginLatitude())
                .endX(request.getDestinationLongitude())
                .endY(request.getDestinationLatitude())
                .reqCoordType("WGS84GEO")
                .resCoordType("WGS84GEO")
                .startName("출발지")
                .endName("목적지")
                .searchOption("0")
                .build();
    }

    private TmapWalkingRouteResponse.Properties extractSummary(
            TmapWalkingRouteResponse response
    ) {
        if (response == null || response.getFeatures() == null) {
            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }

        return response.getFeatures()
                .stream()
                .map(TmapWalkingRouteResponse.Feature::getProperties)
                .filter(properties -> properties != null)
                .filter(properties ->
                        properties.getTotalDistance() != null
                                && properties.getTotalTime() != null
                )
                .findFirst()
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.ROUTE_SEARCH_FAILED
                        )
                );
    }

    private void validateRequest(
            RouteCandidateRequest request
    ) {
        if (request == null
                || request.getOriginLatitude() == null
                || request.getOriginLongitude() == null
                || request.getDestinationLatitude() == null
                || request.getDestinationLongitude() == null) {

            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        validateLatitude(
                request.getOriginLatitude()
        );

        validateLongitude(
                request.getOriginLongitude()
        );

        validateLatitude(
                request.getDestinationLatitude()
        );

        validateLongitude(
                request.getDestinationLongitude()
        );
    }

    private void validateLatitude(
            Double latitude
    ) {
        if (latitude < -90 || latitude > 90) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateLongitude(
            Double longitude
    ) {
        if (longitude < -180 || longitude > 180) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}