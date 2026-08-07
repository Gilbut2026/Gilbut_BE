package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
import com.gilbeot.gilbut.dto.route.RouteCandidateResult;
import com.gilbeot.gilbut.dto.route.transit.request.TransitRouteRequest;
import com.gilbeot.gilbut.dto.route.transit.response.TransitRouteResponse;
import com.gilbeot.gilbut.dto.route.walking.request.WalkingRouteRequest;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteCandidateAggregationService {

    private final WalkingRouteService walkingRouteService;
    private final TransitRouteService transitRouteService;
    private final RouteCandidateMapper routeCandidateMapper;

    public RouteCandidateResult createCandidates(
            RouteCandidateRequest request
    ) {
        List<RouteCandidate> candidates =
                new ArrayList<>();

        WalkingRouteResponse walkingRoute = null;
        TransitRouteResponse transitRoutes = null;

        try {
            walkingRoute =
                    walkingRouteService.search(
                            toWalkingRouteRequest(request)
                    );

            candidates.add(
                    routeCandidateMapper.fromWalkingRoute(
                            walkingRoute
                    )
            );
        } catch (CustomException e) {
            if (e.getErrorCode()
                    != ErrorCode.ROUTE_SEARCH_FAILED) {
                throw e;
            }
        }

        try {
            transitRoutes =
                    transitRouteService.search(
                            toTransitRouteRequest(request)
                    );

            candidates.addAll(
                    routeCandidateMapper.fromTransitRoutes(
                            transitRoutes
                    )
            );
        } catch (CustomException e) {
            if (e.getErrorCode()
                    != ErrorCode.ROUTE_SEARCH_FAILED) {
                throw e;
            }
        }

        if (candidates.isEmpty()) {
            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }

        return RouteCandidateResult.builder()
                .requestId(
                        UUID.randomUUID().toString()
                )
                .candidates(candidates)
                .walkingRoute(walkingRoute)
                .transitRoutes(transitRoutes)
                .build();
    }

    private WalkingRouteRequest toWalkingRouteRequest(
            RouteCandidateRequest request
    ) {
        if (request == null) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        WalkingRouteRequest.RoutePlaceRequest origin =
                new WalkingRouteRequest.RoutePlaceRequest();

        origin.setLatitude(
                request.getOriginLatitude()
        );
        origin.setLongitude(
                request.getOriginLongitude()
        );

        WalkingRouteRequest.RoutePlaceRequest destination =
                new WalkingRouteRequest.RoutePlaceRequest();

        destination.setLatitude(
                request.getDestinationLatitude()
        );
        destination.setLongitude(
                request.getDestinationLongitude()
        );

        WalkingRouteRequest walkingRequest =
                new WalkingRouteRequest();

        walkingRequest.setOrigin(origin);
        walkingRequest.setDestination(destination);

        return walkingRequest;
    }

    private TransitRouteRequest toTransitRouteRequest(
            RouteCandidateRequest request
    ) {
        if (request == null) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        TransitRouteRequest.RoutePlaceRequest origin =
                new TransitRouteRequest.RoutePlaceRequest();

        origin.setLatitude(
                request.getOriginLatitude()
        );
        origin.setLongitude(
                request.getOriginLongitude()
        );

        TransitRouteRequest.RoutePlaceRequest destination =
                new TransitRouteRequest.RoutePlaceRequest();

        destination.setLatitude(
                request.getDestinationLatitude()
        );
        destination.setLongitude(
                request.getDestinationLongitude()
        );

        TransitRouteRequest transitRequest =
                new TransitRouteRequest();

        transitRequest.setOrigin(origin);
        transitRequest.setDestination(destination);
        transitRequest.setDepartureDateTime(
                request.getDepartureDateTime()
        );

        return transitRequest;
    }
}