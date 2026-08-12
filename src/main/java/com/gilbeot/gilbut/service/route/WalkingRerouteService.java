package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.domain.route.RestStopRerouteSegmentType;
import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import com.gilbeot.gilbut.dto.route.walking.request.NavigationRerouteRequest;
import com.gilbeot.gilbut.dto.route.walking.request.RestStopRerouteRequest;
import com.gilbeot.gilbut.dto.route.walking.request.WalkingRouteRequest;
import com.gilbeot.gilbut.dto.route.walking.response.RestStopRerouteItemResponse;
import com.gilbeot.gilbut.dto.route.walking.response.RestStopRerouteResponse;
import com.gilbeot.gilbut.dto.route.walking.response.RestStopRerouteSegmentResponse;
import com.gilbeot.gilbut.dto.route.walking.response.RoutePointResponse;
import com.gilbeot.gilbut.dto.route.walking.response.SelectedRestStopResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteItemResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteSummaryResponse;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingStepResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalkingRerouteService {

    private static final String REST_STOP_ROUTE_ID_PREFIX =
            "walking-rest-stop-";

    private static final String REST_STOP_AVOID_STAIRS_ROUTE_ID_PREFIX =
            "walking-rest-stop-avoid-stairs-";

    private final WalkingRouteService walkingRouteService;

    public WalkingRouteResponse reroute(
            NavigationRerouteRequest request
    ) {
        if (request == null) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return walkingRouteService.search(
                request.toWalkingRouteRequest()
        );
    }

    public RestStopRerouteResponse rerouteViaRestStop(
            RestStopRerouteRequest request
    ) {
        if (request == null) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        WalkingRouteResponse currentToRestStop =
                walkingRouteService.search(
                        toWalkingRouteRequest(
                                request.getCurrentLocation(),
                                request.getRestStop(),
                                request.getRouteOptions()
                        )
                );

        WalkingRouteResponse restStopToDestination =
                walkingRouteService.search(
                        toWalkingRouteRequest(
                                request.getRestStop(),
                                request.getDestination(),
                                request.getRouteOptions()
                        )
                );

        List<RestStopRerouteItemResponse> routes =
                mergeRoutes(
                        request.getRestStop(),
                        currentToRestStop,
                        restStopToDestination
                );

        if (routes.isEmpty()) {
            throw new CustomException(
                    ErrorCode.ROUTE_SEARCH_FAILED
            );
        }

        return RestStopRerouteResponse.builder()
                .routes(routes)
                .build();
    }

    private WalkingRouteRequest toWalkingRouteRequest(
            RestStopRerouteRequest.RoutePlaceRequest origin,
            RestStopRerouteRequest.RestStopRequest destination,
            List<WalkingRouteOption> routeOptions
    ) {
        WalkingRouteRequest request =
                new WalkingRouteRequest();

        request.setOrigin(
                toWalkingPlaceRequest(origin)
        );

        request.setDestination(
                toWalkingPlaceRequest(destination)
        );

        request.setRouteOptions(routeOptions);

        return request;
    }

    private WalkingRouteRequest toWalkingRouteRequest(
            RestStopRerouteRequest.RestStopRequest origin,
            RestStopRerouteRequest.RoutePlaceRequest destination,
            List<WalkingRouteOption> routeOptions
    ) {
        WalkingRouteRequest request =
                new WalkingRouteRequest();

        request.setOrigin(
                toWalkingPlaceRequest(origin)
        );

        request.setDestination(
                toWalkingPlaceRequest(destination)
        );

        request.setRouteOptions(routeOptions);

        return request;
    }

    private WalkingRouteRequest.RoutePlaceRequest
    toWalkingPlaceRequest(
            RestStopRerouteRequest.RoutePlaceRequest source
    ) {
        if (source == null) {
            return null;
        }

        WalkingRouteRequest.RoutePlaceRequest target =
                new WalkingRouteRequest.RoutePlaceRequest();

        target.setPlaceId(source.getPlaceId());
        target.setName(source.getName());
        target.setAddress(source.getAddress());
        target.setLatitude(source.getLatitude());
        target.setLongitude(source.getLongitude());

        return target;
    }

    private WalkingRouteRequest.RoutePlaceRequest
    toWalkingPlaceRequest(
            RestStopRerouteRequest.RestStopRequest source
    ) {
        if (source == null) {
            return null;
        }

        WalkingRouteRequest.RoutePlaceRequest target =
                new WalkingRouteRequest.RoutePlaceRequest();

        target.setPlaceId(source.getFacilityId());
        target.setName(source.getName());
        target.setAddress(source.getAddress());
        target.setLatitude(source.getLatitude());
        target.setLongitude(source.getLongitude());

        return target;
    }

    private List<RestStopRerouteItemResponse> mergeRoutes(
            RestStopRerouteRequest.RestStopRequest restStop,
            WalkingRouteResponse currentToRestStop,
            WalkingRouteResponse restStopToDestination
    ) {
        Map<WalkingRouteOption, WalkingRouteItemResponse>
                destinationRouteByOption =
                toRouteByOption(restStopToDestination);

        List<RestStopRerouteItemResponse> routes =
                new ArrayList<>();

        for (WalkingRouteItemResponse firstRoute
                : routeItems(currentToRestStop)) {
            if (firstRoute == null
                    || firstRoute.getRouteOption() == null) {
                continue;
            }

            WalkingRouteItemResponse secondRoute =
                    destinationRouteByOption.get(
                            firstRoute.getRouteOption()
                    );

            if (secondRoute == null) {
                continue;
            }

            routes.add(
                    toRestStopRerouteItem(
                            restStop,
                            firstRoute,
                            secondRoute
                    )
            );
        }

        return routes;
    }

    private Map<WalkingRouteOption, WalkingRouteItemResponse>
    toRouteByOption(
            WalkingRouteResponse response
    ) {
        Map<WalkingRouteOption, WalkingRouteItemResponse> result =
                new EnumMap<>(WalkingRouteOption.class);

        for (WalkingRouteItemResponse route
                : routeItems(response)) {
            if (route == null
                    || route.getRouteOption() == null) {
                continue;
            }

            result.putIfAbsent(
                    route.getRouteOption(),
                    route
            );
        }

        return result;
    }

    private List<WalkingRouteItemResponse> routeItems(
            WalkingRouteResponse response
    ) {
        if (response == null
                || response.getRoutes() == null) {
            return List.of();
        }

        return response.getRoutes();
    }

    private RestStopRerouteItemResponse toRestStopRerouteItem(
            RestStopRerouteRequest.RestStopRequest restStop,
            WalkingRouteItemResponse currentToRestStop,
            WalkingRouteItemResponse restStopToDestination
    ) {
        return RestStopRerouteItemResponse.builder()
                .routeId(
                        generateRestStopRouteId(
                                currentToRestStop.getRouteOption()
                        )
                )
                .routeOption(
                        currentToRestStop.getRouteOption()
                )
                .restStop(
                        toRestStopResponse(restStop)
                )
                .summary(
                        mergeSummary(
                                currentToRestStop,
                                restStopToDestination
                        )
                )
                .routePoints(
                        mergeRoutePoints(
                                currentToRestStop.getRoutePoints(),
                                restStopToDestination.getRoutePoints()
                        )
                )
                .steps(
                        mergeSteps(
                                currentToRestStop.getSteps(),
                                restStopToDestination.getSteps()
                        )
                )
                .segments(
                        List.of(
                                toSegment(
                                        RestStopRerouteSegmentType
                                                .CURRENT_LOCATION_TO_REST_STOP,
                                        currentToRestStop
                                ),
                                toSegment(
                                        RestStopRerouteSegmentType
                                                .REST_STOP_TO_DESTINATION,
                                        restStopToDestination
                                )
                        )
                )
                .build();
    }

    private SelectedRestStopResponse toRestStopResponse(
            RestStopRerouteRequest.RestStopRequest restStop
    ) {
        return SelectedRestStopResponse.builder()
                .facilityId(restStop.getFacilityId())
                .name(restStop.getName())
                .address(restStop.getAddress())
                .latitude(restStop.getLatitude())
                .longitude(restStop.getLongitude())
                .build();
    }

    private WalkingRouteSummaryResponse mergeSummary(
            WalkingRouteItemResponse currentToRestStop,
            WalkingRouteItemResponse restStopToDestination
    ) {
        int totalDistanceM =
                totalDistanceM(currentToRestStop)
                        + totalDistanceM(restStopToDestination);

        int totalTimeSec =
                totalTimeSec(currentToRestStop)
                        + totalTimeSec(restStopToDestination);

        return WalkingRouteSummaryResponse.builder()
                .totalDistanceM(totalDistanceM)
                .totalTimeSec(totalTimeSec)
                .build();
    }

    private int totalDistanceM(
            WalkingRouteItemResponse route
    ) {
        if (route == null
                || route.getSummary() == null) {
            return 0;
        }

        return valueOrZero(
                route.getSummary()
                        .getTotalDistanceM()
        );
    }

    private int totalTimeSec(
            WalkingRouteItemResponse route
    ) {
        if (route == null
                || route.getSummary() == null) {
            return 0;
        }

        return valueOrZero(
                route.getSummary()
                        .getTotalTimeSec()
        );
    }

    private List<RoutePointResponse> mergeRoutePoints(
            List<RoutePointResponse> first,
            List<RoutePointResponse> second
    ) {
        List<RoutePointResponse> routePoints =
                new ArrayList<>();

        appendRoutePoints(
                routePoints,
                first
        );

        appendRoutePoints(
                routePoints,
                second
        );

        return routePoints;
    }

    private void appendRoutePoints(
            List<RoutePointResponse> target,
            List<RoutePointResponse> source
    ) {
        if (source == null) {
            return;
        }

        for (RoutePointResponse point : source) {
            if (point == null
                    || isSamePoint(
                            lastPoint(target),
                            point
            )) {
                continue;
            }

            target.add(point);
        }
    }

    private RoutePointResponse lastPoint(
            List<RoutePointResponse> points
    ) {
        if (points.isEmpty()) {
            return null;
        }

        return points.get(
                points.size() - 1
        );
    }

    private boolean isSamePoint(
            RoutePointResponse first,
            RoutePointResponse second
    ) {
        if (first == null
                || second == null) {
            return false;
        }

        return Double.compare(
                first.getLatitude(),
                second.getLatitude()
        ) == 0
                && Double.compare(
                first.getLongitude(),
                second.getLongitude()
        ) == 0;
    }

    private List<WalkingStepResponse> mergeSteps(
            List<WalkingStepResponse> first,
            List<WalkingStepResponse> second
    ) {
        List<WalkingStepResponse> steps =
                new ArrayList<>();

        appendSteps(
                steps,
                first
        );

        appendSteps(
                steps,
                second
        );

        return steps;
    }

    private void appendSteps(
            List<WalkingStepResponse> target,
            List<WalkingStepResponse> source
    ) {
        if (source == null) {
            return;
        }

        for (WalkingStepResponse step : source) {
            if (step == null) {
                continue;
            }

            target.add(
                    WalkingStepResponse.builder()
                            .stepIndex(target.size() + 1)
                            .instruction(step.getInstruction())
                            .distanceM(step.getDistanceM())
                            .durationSec(step.getDurationSec())
                            .turnType(step.getTurnType())
                            .pointType(step.getPointType())
                            .points(step.getPoints())
                            .build()
            );
        }
    }

    private RestStopRerouteSegmentResponse toSegment(
            RestStopRerouteSegmentType segmentType,
            WalkingRouteItemResponse route
    ) {
        return RestStopRerouteSegmentResponse.builder()
                .segmentType(segmentType)
                .summary(route.getSummary())
                .routePoints(route.getRoutePoints())
                .steps(route.getSteps())
                .build();
    }

    private int valueOrZero(
            Integer value
    ) {
        return value == null
                ? 0
                : value;
    }

    private String generateRestStopRouteId(
            WalkingRouteOption routeOption
    ) {
        String prefix =
                routeOption == WalkingRouteOption.AVOID_STAIRS
                        ? REST_STOP_AVOID_STAIRS_ROUTE_ID_PREFIX
                        : REST_STOP_ROUTE_ID_PREFIX;

        return prefix
                + UUID.randomUUID();
    }
}
