package com.gilbeot.gilbut.service.chat;

import com.gilbeot.gilbut.client.ai.AiChatClient;
import com.gilbeot.gilbut.client.ai.dto.AiChatAnalyzeResponse;
import com.gilbeot.gilbut.domain.chat.ChatAction;
import com.gilbeot.gilbut.domain.chat.ChatIntent;
import com.gilbeot.gilbut.domain.chat.ChatResponseType;
import com.gilbeot.gilbut.domain.chat.ChatState;
import com.gilbeot.gilbut.dto.chat.request.ChatMessageRequest;
import com.gilbeot.gilbut.dto.chat.response.ChatMessageResponse;
import com.gilbeot.gilbut.dto.chat.response.ChatSessionResponse;
import com.gilbeot.gilbut.dto.place.request.PlaceSearchRequest;
import com.gilbeot.gilbut.dto.place.response.PlaceItemResponse;
import com.gilbeot.gilbut.dto.place.response.PlaceSearchResponse;
import com.gilbeot.gilbut.service.place.PlaceSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatSessionService chatSessionService;

    @Mock
    private AiChatClient aiChatClient;

    @Mock
    private PlaceSearchService placeSearchService;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(
                chatSessionService,
                aiChatClient,
                placeSearchService
        );
    }

    @Test
    @DisplayName("목적지 검색 action이면 장소 검색 결과를 반환한다")
    void searchDestination() {

        ChatSessionResponse session =
                ChatSessionResponse.builder()
                        .sessionId("session-1")
                        .currentState(ChatState.DESTINATION_WAITING)
                        .build();

        AiChatAnalyzeResponse aiResponse =
                AiChatAnalyzeResponse.builder()
                        .intent(ChatIntent.DESTINATION)
                        .action(ChatAction.SEARCH_DESTINATION)
                        .value("수원역")
                        .build();

        PlaceItemResponse place =
                PlaceItemResponse.builder()
                        .placeId("123")
                        .name("수원역")
                        .address("경기도 수원시 팔달구")
                        .latitude(37.2661)
                        .longitude(126.9998)
                        .build();

        PlaceSearchResponse placeResponse =
                PlaceSearchResponse.builder()
                        .places(List.of(place))
                        .pagination(
                                PlaceSearchResponse.PaginationResponse.builder()
                                        .page(1)
                                        .size(5)
                                        .totalCount(1)
                                        .hasNext(false)
                                        .build()
                        )
                        .build();

        when(
                chatSessionService.getOrCreateSession(1L)
        ).thenReturn(session);

        when(
                aiChatClient.analyze(any())
        ).thenReturn(aiResponse);

        when(
                placeSearchService.search(any())
        ).thenReturn(placeResponse);

        ChatMessageRequest request =
                new ChatMessageRequest(
                        "수원역 가고 싶어"
                );

        ChatMessageResponse response =
                chatService.chat(
                        1L,
                        request
                );

        assertThat(response.getSessionId())
                .isEqualTo("session-1");

        assertThat(response.getCurrentState())
                .isEqualTo(ChatState.DESTINATION_WAITING);

        assertThat(response.getResponseType())
                .isEqualTo(ChatResponseType.PLACE_CANDIDATES);

        assertThat(response.getPlaces())
                .hasSize(1);

        assertThat(
                response.getPlaces()
                        .get(0)
                        .getName()
        ).isEqualTo("수원역");

        ArgumentCaptor<PlaceSearchRequest> captor =
                ArgumentCaptor.forClass(
                        PlaceSearchRequest.class
                );

        verify(
                placeSearchService
        ).search(
                captor.capture()
        );

        assertThat(
                captor.getValue()
                        .getKeyword()
        ).isEqualTo("수원역");

        assertThat(
                captor.getValue()
                        .getSize()
        ).isEqualTo("5");
    }
}