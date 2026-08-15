package com.gilbeot.gilbut.service.history;

import com.gilbeot.gilbut.domain.chat.ChatSession;
import com.gilbeot.gilbut.domain.history.RouteHistory;
import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.dto.drt.DrtGuideResponse;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
import com.gilbeot.gilbut.dto.route.RouteMetrics;
import com.gilbeot.gilbut.dto.route.RouteRecommendationItem;
import com.gilbeot.gilbut.dto.route.RouteRecommendationResult;
import com.gilbeot.gilbut.dto.route.history.response.RouteHistoryDetailResponse;
import com.gilbeot.gilbut.dto.route.history.response.RouteHistoryResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.repository.ChatSessionRepository;
import com.gilbeot.gilbut.repository.RouteHistoryRepository;
import com.gilbeot.gilbut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteHistoryService {

    private static final String DEFAULT_ORIGIN_NAME = "출발지";
    private static final String DEFAULT_DESTINATION_NAME = "목적지";
    private static final double COORDINATE_TOLERANCE = 0.00001;

    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final RouteHistoryRepository routeHistoryRepository;

    public List<RouteHistoryResponse> getHistories(
            Long userId
    ) {
        return routeHistoryRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(RouteHistoryResponse::from)
                .toList();
    }

    public RouteHistoryDetailResponse getHistory(
            Long userId,
            Long historyId
    ) {
        RouteHistory routeHistory =
                routeHistoryRepository
                        .findByIdAndUserId(
                                historyId,
                                userId
                        )
                        .orElseThrow(
                                () -> new CustomException(
                                        ErrorCode.ROUTE_HISTORY_NOT_FOUND
                                )
                        );

        return RouteHistoryDetailResponse.from(
                routeHistory
        );
    }

    @Transactional
    public void deleteHistory(
            Long userId,
            Long historyId
    ) {
        RouteHistory routeHistory =
                routeHistoryRepository
                        .findByIdAndUserId(
                                historyId,
                                userId
                        )
                        .orElseThrow(
                                () -> new CustomException(
                                        ErrorCode.ROUTE_HISTORY_NOT_FOUND
                                )
                        );

        routeHistoryRepository.delete(routeHistory);
    }

    @Transactional
    public void saveRecommendation(
            Long userId,
            RouteCandidateRequest request,
            RouteRecommendationResult result
    ) {
        RouteRecommendationItem topRecommendation =
                extractTopRecommendation(result);

        DrtGuideResponse drtGuide =
                result.getDrtGuide();

        boolean drtRecommended =
                drtGuide != null
                        && Boolean.TRUE.equals(
                        drtGuide.getShow()
                );

        if (topRecommendation == null
                && !drtRecommended) {
            return;
        }

        User user =
                userRepository.findById(userId)
                        .orElseThrow(
                                () -> new CustomException(
                                        ErrorCode.USER_NOT_FOUND
                                )
                        );

        ChatSession session =
                findMatchingSession(
                        userId,
                        request
                );

        RouteCandidate candidate =
                topRecommendation == null
                        ? null
                        : topRecommendation.getCandidate();

        RouteMetrics metrics =
                candidate == null
                        ? null
                        : candidate.getMetrics();

        RouteHistory routeHistory =
                RouteHistory.builder()
                        .user(user)
                        .requestId(
                                result.getRequestId()
                        )
                        .originName(
                                resolveOriginName(session)
                        )
                        .originAddress(
                                session == null
                                        ? null
                                        : session.getOriginAddress()
                        )
                        .originLatitude(
                                request.getOriginLatitude()
                        )
                        .originLongitude(
                                request.getOriginLongitude()
                        )
                        .destinationName(
                                resolveDestinationName(session)
                        )
                        .destinationAddress(
                                session == null
                                        ? null
                                        : session.getDestinationAddress()
                        )
                        .destinationLatitude(
                                request.getDestinationLatitude()
                        )
                        .destinationLongitude(
                                request.getDestinationLongitude()
                        )
                        .departureDateTime(
                                request.getDepartureDateTime()
                        )
                        .recommendedRouteId(
                                topRecommendation == null
                                        ? null
                                        : topRecommendation.getRouteId()
                        )
                        .recommendationReason(
                                topRecommendation == null
                                        ? null
                                        : topRecommendation.getRecommendationReason()
                        )
                        .recommendedRouteType(
                                candidate == null
                                        ? null
                                        : candidate.getRouteType()
                        )
                        .recommendedRouteOption(
                                candidate == null
                                        ? null
                                        : candidate.getRouteOption()
                        )
                        .totalTimeSec(
                                metrics == null
                                        ? null
                                        : metrics.getTotalTimeSec()
                        )
                        .totalWalkTimeSec(
                                metrics == null
                                        ? null
                                        : metrics.getTotalWalkTimeSec()
                        )
                        .totalWalkDistanceM(
                                metrics == null
                                        ? null
                                        : metrics.getTotalWalkDistanceM()
                        )
                        .transferCount(
                                metrics == null
                                        ? null
                                        : metrics.getTransferCount()
                        )
                        .drtRecommended(
                                drtRecommended
                        )
                        .drtServiceArea(
                                drtRecommended
                                        ? drtGuide.getServiceArea()
                                        : null
                        )
                        .build();

        routeHistoryRepository.save(
                routeHistory
        );
    }

    private ChatSession findMatchingSession(
            Long userId,
            RouteCandidateRequest request
    ) {
        return chatSessionRepository
                .findByUserId(userId)
                .filter(session ->
                        isSameCoordinate(
                                session.getOriginLatitude(),
                                request.getOriginLatitude()
                        )
                                && isSameCoordinate(
                                session.getOriginLongitude(),
                                request.getOriginLongitude()
                        )
                                && isSameCoordinate(
                                session.getDestinationLatitude(),
                                request.getDestinationLatitude()
                        )
                                && isSameCoordinate(
                                session.getDestinationLongitude(),
                                request.getDestinationLongitude()
                        )
                )
                .orElse(null);
    }

    private boolean isSameCoordinate(
            Double first,
            Double second
    ) {
        if (first == null || second == null) {
            return false;
        }

        return Math.abs(first - second)
                <= COORDINATE_TOLERANCE;
    }

    private RouteRecommendationItem extractTopRecommendation(
            RouteRecommendationResult result
    ) {
        if (result == null
                || result.getRecommendations() == null
                || result.getRecommendations().isEmpty()) {
            return null;
        }

        return result.getRecommendations()
                .get(0);
    }

    private String resolveOriginName(
            ChatSession session
    ) {
        if (session != null
                && StringUtils.hasText(
                session.getOriginName()
        )) {
            return session.getOriginName()
                    .trim();
        }

        return DEFAULT_ORIGIN_NAME;
    }

    private String resolveDestinationName(
            ChatSession session
    ) {
        if (session != null
                && StringUtils.hasText(
                session.getDestinationName()
        )) {
            return session.getDestinationName()
                    .trim();
        }

        return DEFAULT_DESTINATION_NAME;
    }
}