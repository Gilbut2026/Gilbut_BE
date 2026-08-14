package com.gilbeot.gilbut.service.history;

import com.gilbeot.gilbut.domain.chat.ChatSession;
import com.gilbeot.gilbut.domain.chat.ChatState;
import com.gilbeot.gilbut.domain.history.RouteHistory;
import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.dto.drt.DrtAvailability;
import com.gilbeot.gilbut.dto.drt.DrtGuideResponse;
import com.gilbeot.gilbut.dto.drt.DrtServiceArea;
import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
import com.gilbeot.gilbut.dto.route.RouteMetrics;
import com.gilbeot.gilbut.dto.route.RouteRecommendationItem;
import com.gilbeot.gilbut.dto.route.RouteRecommendationResult;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.repository.ChatSessionRepository;
import com.gilbeot.gilbut.repository.RouteHistoryRepository;
import com.gilbeot.gilbut.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteHistoryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private RouteHistoryRepository routeHistoryRepository;

    private RouteHistoryService routeHistoryService;

    @BeforeEach
    void setUp() {
        routeHistoryService =
                new RouteHistoryService(
                        userRepository,
                        chatSessionRepository,
                        routeHistoryRepository
                );
    }

    @Test
    @DisplayName("추천 1순위와 DRT 안내 정보를 상담 이력으로 저장한다")
    void saveRecommendation() {
        Long userId = 1L;

        LocalDateTime departureDateTime =
                LocalDateTime.of(
                        2026,
                        8,
                        13,
                        10,
                        30
                );

        User user = User.builder()
                .id(userId)
                .username("tester")
                .providerId("kakao-1")
                .build();

        ChatSession session = ChatSession.builder()
                .user(user)
                .sessionId("session-1")
                .currentState(
                        ChatState.ROUTE_CALCULATING
                )
                .originName("현재 위치")
                .originLatitude(37.2636)
                .originLongitude(127.0286)
                .destinationName("○○병원")
                .destinationAddress(
                        "수원시 팔달구 ○○로 12"
                )
                .destinationLatitude(37.2790)
                .destinationLongitude(127.0470)
                .departureDateTime(departureDateTime)
                .build();

        RouteCandidateRequest request =
                RouteCandidateRequest.builder()
                        .originLatitude(37.2636)
                        .originLongitude(127.0286)
                        .destinationLatitude(37.2790)
                        .destinationLongitude(127.0470)
                        .departureDateTime(departureDateTime)
                        .build();

        RouteCandidate candidate =
                RouteCandidate.builder()
                        .routeId("walking-1")
                        .routeType(RouteType.WALKING)
                        .routeOption(
                                WalkingRouteOption.DEFAULT
                        )
                        .providerRank(1)
                        .metrics(
                                RouteMetrics.builder()
                                        .totalTimeSec(900)
                                        .totalWalkTimeSec(900)
                                        .totalWalkDistanceM(1000)
                                        .transferCount(0)
                                        .build()
                        )
                        .build();

        RouteRecommendationItem recommendation =
                RouteRecommendationItem.builder()
                        .routeId("walking-1")
                        .candidate(candidate)
                        .score(92.0)
                        .rank(1)
                        .build();

        DrtGuideResponse drtGuide =
                DrtGuideResponse.builder()
                        .show(true)
                        .serviceName("수원 똑버스")
                        .serviceArea(
                                DrtServiceArea.GWANGGYO
                        )
                        .serviceAreaName("광교1·2동")
                        .availability(
                                DrtAvailability.CHECK_REQUIRED
                        )
                        .message("이용 안내")
                        .build();

        RouteRecommendationResult result =
                RouteRecommendationResult.builder()
                        .requestId(
                                "641ef968-a406-4152-b6c2-5fd4fc6d4d58"
                        )
                        .recommendations(
                                List.of(recommendation)
                        )
                        .drtGuide(drtGuide)
                        .build();

        when(
                userRepository.findById(userId)
        ).thenReturn(Optional.of(user));

        when(
                chatSessionRepository.findByUserId(userId)
        ).thenReturn(Optional.of(session));

        routeHistoryService.saveRecommendation(
                userId,
                request,
                result
        );

        ArgumentCaptor<RouteHistory> captor =
                ArgumentCaptor.forClass(
                        RouteHistory.class
                );

        verify(routeHistoryRepository).save(
                captor.capture()
        );

        RouteHistory saved = captor.getValue();

        assertThat(saved.getOriginName())
                .isEqualTo("현재 위치");

        assertThat(saved.getDestinationName())
                .isEqualTo("○○병원");

        assertThat(saved.getDestinationAddress())
                .isEqualTo("수원시 팔달구 ○○로 12");

        assertThat(saved.getRecommendedRouteId())
                .isEqualTo("walking-1");

        assertThat(saved.getRecommendedRouteType())
                .isEqualTo(RouteType.WALKING);

        assertThat(saved.getRecommendedRouteOption())
                .isEqualTo(WalkingRouteOption.DEFAULT);

        assertThat(saved.getTotalTimeSec())
                .isEqualTo(900);

        assertThat(saved.isDrtRecommended())
                .isTrue();

        assertThat(saved.getDrtServiceArea())
                .isEqualTo(DrtServiceArea.GWANGGYO);
    }

    @Test
    @DisplayName("상담 이력을 삭제한다")
    void deleteHistory() {
        Long userId = 1L;
        Long historyId = 10L;

        User user = User.builder()
                .id(userId)
                .username("tester")
                .providerId("kakao-1")
                .build();

        RouteHistory routeHistory =
                RouteHistory.builder()
                        .id(historyId)
                        .user(user)
                        .requestId(
                                "641ef968-a406-4152-b6c2-5fd4fc6d4d58"
                        )
                        .originName("현재 위치")
                        .originLatitude(37.2636)
                        .originLongitude(127.0286)
                        .destinationName("○○병원")
                        .destinationLatitude(37.2790)
                        .destinationLongitude(127.0470)
                        .drtRecommended(false)
                        .build();

        when(
                routeHistoryRepository.findByIdAndUserId(
                        historyId,
                        userId
                )
        ).thenReturn(Optional.of(routeHistory));

        routeHistoryService.deleteHistory(
                userId,
                historyId
        );

        verify(routeHistoryRepository).delete(
                routeHistory
        );
    }

    @Test
    @DisplayName("본인 이력이 아니면 삭제하지 않는다")
    void deleteHistoryNotFound() {
        Long userId = 1L;
        Long historyId = 10L;

        when(
                routeHistoryRepository.findByIdAndUserId(
                        historyId,
                        userId
                )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> routeHistoryService.deleteHistory(
                        userId,
                        historyId
                )
        ).isInstanceOf(CustomException.class);
    }
}
