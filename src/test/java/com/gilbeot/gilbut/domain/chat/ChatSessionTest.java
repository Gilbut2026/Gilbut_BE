package com.gilbeot.gilbut.domain.chat;

import com.gilbeot.gilbut.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ChatSessionTest {

    @Test
    @DisplayName("대화 세션은 목적지 대기 상태로 생성된다")
    void createChatSession() {

        User user = User.builder()
                .id(1L)
                .username("test-user")
                .providerId("kakao-test")
                .build();

        ChatSession session =
                ChatSession.create(user);

        assertThat(session.getCurrentState())
                .isEqualTo(ChatState.DESTINATION_WAITING);

        assertThat(session.getSessionId())
                .isNotBlank();
    }

    @Test
    @DisplayName("목적지를 확정하면 출발지 확인 상태로 변경된다")
    void confirmDestination() {

        User user = User.builder()
                .id(1L)
                .username("test-user")
                .providerId("kakao-test")
                .build();

        ChatSession session =
                ChatSession.create(user);

        session.confirmDestination(
                "12345",
                "수원역",
                "경기도 수원시 팔달구 덕영대로 924",
                37.2661,
                126.9998
        );

        assertThat(session.getCurrentState())
                .isEqualTo(ChatState.ORIGIN_CONFIRMATION);

        assertThat(session.getDestinationName())
                .isEqualTo("수원역");
    }

    @Test
    @DisplayName("현재 위치를 출발지로 저장할 수 있다")
    void confirmCurrentLocationOrigin() {

        User user = User.builder()
                .id(1L)
                .username("test-user")
                .providerId("kakao-test")
                .build();

        ChatSession session =
                ChatSession.create(user);

        session.confirmDestination(
                "12345",
                "아주대학교병원",
                "경기도 수원시 영통구 월드컵로 164",
                37.279,
                127.047
        );

        session.confirmOrigin(
                OriginType.CURRENT_LOCATION,
                null,
                "현재 위치",
                null,
                37.2636,
                127.0286
        );

        session.moveToDepartureTimeConfirmation();

        assertThat(session.getOriginType())
                .isEqualTo(OriginType.CURRENT_LOCATION);

        assertThat(session.getOriginName())
                .isEqualTo("현재 위치");

        assertThat(session.getOriginAddress())
                .isNull();

        assertThat(session.getOriginLatitude())
                .isEqualTo(37.2636);

        assertThat(session.getOriginLongitude())
                .isEqualTo(127.0286);

        assertThat(session.getCurrentState())
                .isEqualTo(
                        ChatState.DEPARTURE_TIME_CONFIRMATION
                );
    }

    @Test
    @DisplayName("출발 시간을 확정하면 시간이 저장되고 경로 계산 단계로 이동한다")
    void confirmDepartureTime() {

        User user = User.builder()
                .id(1L)
                .username("test-user")
                .providerId("kakao-test")
                .build();

        ChatSession session =
                ChatSession.create(user);

        session.confirmDestination(
                "12345",
                "아주대학교병원",
                "경기도 수원시 영통구 월드컵로 164",
                37.279,
                127.047
        );

        session.confirmOrigin(
                OriginType.CURRENT_LOCATION,
                null,
                "현재 위치",
                null,
                37.2636,
                127.0286
        );

        session.moveToDepartureTimeConfirmation();

        LocalDateTime departureDateTime =
                LocalDateTime.of(
                        2026,
                        8,
                        7,
                        14,
                        0
                );

        session.confirmDepartureTime(
                departureDateTime
        );

        assertThat(session.getDepartureDateTime())
                .isEqualTo(departureDateTime);

        assertThat(session.getCurrentState())
                .isEqualTo(
                        ChatState.ROUTE_CALCULATING
                );
    }

    @Test
    @DisplayName("세션 초기화 시 이동 Context와 출발 시간이 제거된다")
    void resetChatSession() {

        User user = User.builder()
                .id(1L)
                .username("test-user")
                .providerId("kakao-test")
                .build();

        ChatSession session =
                ChatSession.create(user);

        String previousSessionId =
                session.getSessionId();

        session.confirmDestination(
                "12345",
                "수원역",
                "경기도 수원시 팔달구 덕영대로 924",
                37.2661,
                126.9998
        );

        session.confirmOrigin(
                OriginType.PLACE,
                "67890",
                "수원시청",
                "경기도 수원시 팔달구 효원로 241",
                37.2636,
                127.0286
        );

        session.moveToDepartureTimeConfirmation();

        LocalDateTime departureDateTime =
                LocalDateTime.of(
                        2026,
                        8,
                        8,
                        10,
                        0
                );

        session.confirmDepartureTime(
                departureDateTime
        );

        session.startRouteCalculation(
                "request-1"
        );

        assertThat(session.getDepartureDateTime())
                .isEqualTo(departureDateTime);

        assertThat(session.getActiveRequestId())
                .isEqualTo("request-1");

        session.reset();

        assertThat(session.getSessionId())
                .isNotEqualTo(previousSessionId);

        assertThat(session.getCurrentState())
                .isEqualTo(ChatState.DESTINATION_WAITING);

        assertThat(session.getDestinationName())
                .isNull();

        assertThat(session.getOriginType())
                .isNull();

        assertThat(session.getOriginName())
                .isNull();

        assertThat(session.getDepartureDateTime())
                .isNull();

        assertThat(session.getActiveRequestId())
                .isNull();

        assertThat(session.getSelectedRouteId())
                .isNull();
    }
}