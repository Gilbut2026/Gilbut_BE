package com.gilbeot.gilbut.client.ai;

import com.gilbeot.gilbut.client.ai.dto.AiChatAnalyzeRequest;
import com.gilbeot.gilbut.client.ai.dto.AiChatAnalyzeResponse;
import com.gilbeot.gilbut.domain.chat.ChatAction;
import com.gilbeot.gilbut.domain.chat.ChatIntent;
import com.gilbeot.gilbut.domain.chat.ChatState;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpAiChatClientTest {

    private static final String AI_CHAT_URL =
            "http://localhost:8000/chat";

    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    private HttpAiChatClient httpAiChatClient;

    @BeforeEach
    void setUp() {
        restTemplate =
                new RestTemplate();

        mockServer =
                MockRestServiceServer
                        .bindTo(restTemplate)
                        .build();

        httpAiChatClient =
                new HttpAiChatClient(
                        restTemplate
                );

        ReflectionTestUtils.setField(
                httpAiChatClient,
                "aiChatUrl",
                AI_CHAT_URL
        );
    }

    @Test
    @DisplayName(
            "AI 채팅 서버에 계약 형식으로 요청하고 목적지 검색 응답을 정상 변환한다"
    )
    void analyzesDestinationResponse() {
        mockServer.expect(
                        requestTo(
                                AI_CHAT_URL
                        )
                )
                .andExpect(
                        method(
                                HttpMethod.POST
                        )
                )
                .andExpect(
                        content().contentType(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        content().json(
                                """
                                {
                                  "sessionId": "session-1",
                                  "state": "DESTINATION_WAITING",
                                  "message": "수원역 갈래",
                                  "context": {
                                    "destination": null,
                                    "origin": null
                                  }
                                }
                                """
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "intent": "DESTINATION",
                                  "action": "SEARCH_DESTINATION",
                                  "value": "수원역",
                                  "referencePlace": null
                                }
                                """,
                                MediaType.APPLICATION_JSON
                        )
                );

        AiChatAnalyzeRequest request =
                request(
                        "수원역 갈래"
                );

        AiChatAnalyzeResponse response =
                httpAiChatClient.analyze(
                        request
                );

        assertThat(
                response.getIntent()
        ).isEqualTo(
                ChatIntent.DESTINATION
        );

        assertThat(
                response.getAction()
        ).isEqualTo(
                ChatAction.SEARCH_DESTINATION
        );

        assertThat(
                response.getValue()
        ).isEqualTo(
                "수원역"
        );

        assertThat(
                response.getReferencePlace()
        ).isNull();

        mockServer.verify();
    }

    @Test
    @DisplayName(
            "특정 장소 주변 검색 응답의 referencePlace을 정상 변환한다"
    )
    void analyzesNearbyPlaceResponseWithReferencePlace() {
        mockServer.expect(
                        requestTo(
                                AI_CHAT_URL
                        )
                )
                .andExpect(
                        method(
                                HttpMethod.POST
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "intent": "FACILITY",
                                  "action": "SEARCH_NEARBY_PLACE",
                                  "value": "병원",
                                  "referencePlace": "수원역"
                                }
                                """,
                                MediaType.APPLICATION_JSON
                        )
                );

        AiChatAnalyzeResponse response =
                httpAiChatClient.analyze(
                        request(
                                "수원역 근처 병원 찾아줘"
                        )
                );

        assertThat(
                response.getIntent()
        ).isEqualTo(
                ChatIntent.FACILITY
        );

        assertThat(
                response.getAction()
        ).isEqualTo(
                ChatAction.SEARCH_NEARBY_PLACE
        );

        assertThat(
                response.getValue()
        ).isEqualTo(
                "병원"
        );

        assertThat(
                response.getReferencePlace()
        ).isEqualTo(
                "수원역"
        );

        mockServer.verify();
    }

    @Test
    @DisplayName(
            "AI 채팅 서버가 빈 응답을 반환하면 유효하지 않은 응답 예외를 발생시킨다"
    )
    void throwsWhenResponseBodyIsEmpty() {
        mockServer.expect(
                        requestTo(
                                AI_CHAT_URL
                        )
                )
                .andExpect(
                        method(
                                HttpMethod.POST
                        )
                )
                .andRespond(
                        withSuccess(
                                "",
                                MediaType.APPLICATION_JSON
                        )
                );

        assertThatThrownBy(
                () ->
                        httpAiChatClient.analyze(
                                request(
                                        "수원역 갈래"
                                )
                        )
        )
                .isInstanceOf(
                        CustomException.class
                )
                .satisfies(
                        throwable ->
                                assertThat(
                                        ((CustomException)
                                                throwable)
                                                .getErrorCode()
                                ).isEqualTo(
                                        ErrorCode
                                                .AI_CHAT_RESPONSE_INVALID
                                )
                );

        mockServer.verify();
    }

    @Test
    @DisplayName(
            "AI 채팅 서버 호출에 실패하면 AI_CHAT_FAILED 예외를 발생시킨다"
    )
    void throwsWhenAiServerCallFails() {
        mockServer.expect(
                        requestTo(
                                AI_CHAT_URL
                        )
                )
                .andExpect(
                        method(
                                HttpMethod.POST
                        )
                )
                .andRespond(
                        withStatus(
                                HttpStatus
                                        .INTERNAL_SERVER_ERROR
                        )
                                .contentType(
                                        MediaType
                                                .APPLICATION_JSON
                                )
                                .body(
                                        """
                                        {
                                          "message": "internal server error"
                                        }
                                        """
                                )
                );

        assertThatThrownBy(
                () ->
                        httpAiChatClient.analyze(
                                request(
                                        "수원역 갈래"
                                )
                        )
        )
                .isInstanceOf(
                        CustomException.class
                )
                .satisfies(
                        throwable ->
                                assertThat(
                                        ((CustomException)
                                                throwable)
                                                .getErrorCode()
                                ).isEqualTo(
                                        ErrorCode
                                                .AI_CHAT_FAILED
                                )
                );

        mockServer.verify();
    }

    @Test
    @DisplayName(
            "AI 채팅 서버 URL이 설정되지 않으면 AI_SERVER_UNAVAILABLE 예외를 발생시킨다"
    )
    void throwsWhenAiChatUrlIsMissing() {
        ReflectionTestUtils.setField(
                httpAiChatClient,
                "aiChatUrl",
                ""
        );

        assertThatThrownBy(
                () ->
                        httpAiChatClient.analyze(
                                request(
                                        "수원역 갈래"
                                )
                        )
        )
                .isInstanceOf(
                        CustomException.class
                )
                .satisfies(
                        throwable ->
                                assertThat(
                                        ((CustomException)
                                                throwable)
                                                .getErrorCode()
                                ).isEqualTo(
                                        ErrorCode
                                                .AI_SERVER_UNAVAILABLE
                                )
                );
    }

    private AiChatAnalyzeRequest request(
            String message
    ) {
        return AiChatAnalyzeRequest.builder()
                .sessionId(
                        "session-1"
                )
                .state(
                        ChatState.DESTINATION_WAITING
                )
                .message(message)
                .context(
                        AiChatAnalyzeRequest.Context
                                .builder()
                                .destination(null)
                                .origin(null)
                                .build()
                )
                .build();
    }
}