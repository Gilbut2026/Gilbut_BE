package com.gilbeot.gilbut.domain.chat;

import com.gilbeot.gilbut.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.gilbeot.gilbut.dto.chat.request.PlaceConfirmationRequest;
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
    @DisplayName("세션 초기화 시 이동 Context가 제거된다")
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
                "67890",
                "수원시청",
                "경기도 수원시 팔달구 효원로 241",
                37.2636,
                127.0286
        );

        session.startRouteCalculation(
                "request-1"
        );

        session.reset();

        assertThat(session.getSessionId())
                .isNotEqualTo(previousSessionId);

        assertThat(session.getCurrentState())
                .isEqualTo(ChatState.DESTINATION_WAITING);

        assertThat(session.getDestinationName())
                .isNull();

        assertThat(session.getOriginName())
                .isNull();

        assertThat(session.getActiveRequestId())
                .isNull();

        assertThat(session.getSelectedRouteId())
                .isNull();
    }


}