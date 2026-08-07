package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.client.tmap.TmapTransitRouteClient;
import com.gilbeot.gilbut.client.tmap.dto.transit.TmapTransitRouteRequest;
import com.gilbeot.gilbut.client.tmap.dto.transit.TmapTransitRouteResponse;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
import com.gilbeot.gilbut.dto.route.RouteCandidateResult;
import com.gilbeot.gilbut.dto.route.RouteMetrics;
import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteCandidateService {

    private static final int DEFAULT_COUNT = 5;

    private static final DateTimeFormatter SEARCH_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final TmapTransitRouteClient tmapTransitRouteClient;

    public RouteCandidateResult createCandidates(
            RouteCandidateRequest request
    ) {
        validateRequest(request);

        TmapTransitRouteRequest tmapRequest =
                toTmapRequest(request);

        TmapTransitRouteResponse tmapResponse =
                tmapTransitRouteClient.search(tmapRequest);

        String requestId =
                UUID.randomUUID().toString();

        List<RouteCandidate> candidates =
                toCandidates(tmapResponse);

        return RouteCandidateResult.builder()
                .requestId(requestId)
                .candidates(candidates)
                .build();
    }

    private TmapTransitRouteRequest toTmapRequest(
            RouteCandidateRequest request
    ) {
        return TmapTransitRouteRequest.builder()
                .startX(
                        String.valueOf(
                                request.getOriginLongitude()
                        )
                )
                .startY(
                        String.valueOf(
                                request.getOriginLatitude()
                        )
                )
                .endX(
                        String.valueOf(
                                request.getDestinationLongitude()
                        )
                )
                .endY(
                        String.valueOf(
                                request.getDestinationLatitude()
                        )
                )
                .count(DEFAULT_COUNT)
                .lang(0)
                .format("json")
                .searchDttm(
                        request.getDepartureDateTime()
                                .format(
                                        SEARCH_DATETIME_FORMATTER
                                )
                )
                .build();
    }

    private List<RouteCandidate> toCandidates(
            TmapTransitRouteResponse response
    ) {
        List<TmapTransitRouteResponse.Itinerary> itineraries =
                extractItineraries(response);

        List<RouteCandidate> candidates =
                new ArrayList<>();

        for (
                int index = 0;
                index < itineraries.size();
                index++
        ) {
            TmapTransitRouteResponse.Itinerary itinerary =
                    itineraries.get(index);

            int rank = index + 1;

            candidates.add(
                    RouteCandidate.builder()
                            .routeId("transit-" + rank)
                            .routeType(RouteType.TRANSIT)
                            .providerRank(rank)
                            .metrics(
                                    RouteMetrics.builder()
                                            .totalTimeSec(
                                                    itinerary.getTotalTime()
                                            )
                                            .totalWalkTimeSec(
                                                    itinerary.getTotalWalkTime()
                                            )
                                            .totalWalkDistanceM(
                                                    itinerary.getTotalWalkDistance()
                                            )
                                            .transferCount(
                                                    itinerary.getTransferCount()
                                            )
                                            .build()
                            )
                            .build()
            );
        }

        return candidates;
    }

    private List<TmapTransitRouteResponse.Itinerary>
    extractItineraries(
            TmapTransitRouteResponse response
    ) {
        if (response == null
                || response.getMetaData() == null
                || response.getMetaData().getPlan() == null
                || response.getMetaData()
                .getPlan()
                .getItineraries() == null) {

            return List.of();
        }

        return response.getMetaData()
                .getPlan()
                .getItineraries();
    }

    private void validateRequest(
            RouteCandidateRequest request
    ) {
        if (request == null
                || request.getOriginLatitude() == null
                || request.getOriginLongitude() == null
                || request.getDestinationLatitude() == null
                || request.getDestinationLongitude() == null
                || request.getDepartureDateTime() == null) {

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