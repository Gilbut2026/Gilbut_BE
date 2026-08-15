package com.gilbeot.gilbut.service.chat;

import com.gilbeot.gilbut.client.ai.AiChatClient;
import com.gilbeot.gilbut.client.ai.dto.AiChatAnalyzeResponse;
import com.gilbeot.gilbut.domain.chat.ChatAction;
import com.gilbeot.gilbut.domain.chat.ChatIntent;
import com.gilbeot.gilbut.domain.chat.ChatResponseType;
import com.gilbeot.gilbut.domain.chat.ChatState;
import com.gilbeot.gilbut.dto.chat.request.ChatMessageRequest;
import com.gilbeot.gilbut.dto.chat.request.PlaceConfirmationRequest;
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
import static org.mockito.Mockito.verifyNoInteractions;

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
                        .currentState(
                                ChatState.DESTINATION_WAITING
                        )
                        .build();

        AiChatAnalyzeResponse aiResponse =
                AiChatAnalyzeResponse.builder()
                        .intent(
                                ChatIntent.DESTINATION
                        )
                        .action(
                                ChatAction.SEARCH_DESTINATION
                        )
                        .value("수원역")
                        .build();

        PlaceItemResponse place =
                PlaceItemResponse.builder()
                        .placeId("123")
                        .name("수원역")
                        .address(
                                "경기도 수원시 팔달구"
                        )
                        .latitude(37.2661)
                        .longitude(126.9998)
                        .build();

        PlaceSearchResponse placeResponse =
                PlaceSearchResponse.builder()
                        .places(
                                List.of(place)
                        )
                        .pagination(
                                PlaceSearchResponse
                                        .PaginationResponse
                                        .builder()
                                        .page(1)
                                        .size(5)
                                        .totalCount(1)
                                        .hasNext(false)
                                        .build()
                        )
                        .build();

        when(
                chatSessionService
                        .getOrCreateSession(1L)
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

        assertThat(
                response.getSessionId()
        ).isEqualTo(
                "session-1"
        );

        assertThat(
                response.getCurrentState()
        ).isEqualTo(
                ChatState.DESTINATION_WAITING
        );

        assertThat(
                response.getResponseType()
        ).isEqualTo(
                ChatResponseType.PLACE_CANDIDATES
        );

        assertThat(
                response.getPlaces()
        ).hasSize(1);

        assertThat(
                response.getPlaces()
                        .get(0)
                        .getName()
        ).isEqualTo(
                "수원역"
        );

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
        ).isEqualTo(
                "수원역"
        );

        assertThat(
                captor.getValue()
                        .getSize()
        ).isEqualTo(
                "5"
        );
    }

    @Test
    @DisplayName("주변 장소 검색이면 현재 위치 기준으로 장소를 검색한다")
    void searchNearbyPlace() {

        ChatSessionResponse session =
                ChatSessionResponse.builder()
                        .sessionId("session-1")
                        .currentState(
                                ChatState.DESTINATION_WAITING
                        )
                        .build();

        AiChatAnalyzeResponse aiResponse =
                AiChatAnalyzeResponse.builder()
                        .intent(
                                ChatIntent.FACILITY
                        )
                        .action(
                                ChatAction.SEARCH_NEARBY_PLACE
                        )
                        .value("이비인후과")
                        .build();

        PlaceItemResponse place =
                PlaceItemResponse.builder()
                        .placeId("123")
                        .name("수원이비인후과")
                        .address("경기도 수원시")
                        .latitude(37.263)
                        .longitude(127.028)
                        .build();

        PlaceSearchResponse placeResponse =
                PlaceSearchResponse.builder()
                        .places(List.of(place))
                        .build();

        when(
                chatSessionService
                        .getOrCreateSession(1L)
        ).thenReturn(session);

        when(
                aiChatClient.analyze(any())
        ).thenReturn(aiResponse);

        when(
                placeSearchService.search(any())
        ).thenReturn(placeResponse);

        ChatMessageRequest request =
                new ChatMessageRequest(
                        "근처 이비인후과 찾아줘",
                        37.2636,
                        127.0286
                );

        ChatMessageResponse response =
                chatService.chat(
                        1L,
                        request
                );

        assertThat(
                response.getResponseType()
        ).isEqualTo(
                ChatResponseType.PLACE_CANDIDATES
        );

        assertThat(
                response.getPlaces()
        ).hasSize(1);

        ArgumentCaptor<PlaceSearchRequest> captor =
                ArgumentCaptor.forClass(
                        PlaceSearchRequest.class
                );

        verify(
                placeSearchService
        ).search(
                captor.capture()
        );

        PlaceSearchRequest captured =
                captor.getValue();

        assertThat(
                captured.getKeyword()
        ).isEqualTo(
                "이비인후과"
        );

        assertThat(
                captured.getLat()
        ).isEqualTo(
                "37.2636"
        );

        assertThat(
                captured.getLon()
        ).isEqualTo(
                "127.0286"
        );

        assertThat(
                captured.getRadiusKm()
        ).isEqualTo(
                "5"
        );

        assertThat(
                captured.getSize()
        ).isEqualTo(
                "5"
        );
    }

    @Test
    @DisplayName("사용자가 장소를 확정하면 대화 세션에 목적지를 저장한다")
    void confirmDestination() {

        PlaceConfirmationRequest request =
                new PlaceConfirmationRequest(
                        "12345",
                        "아주대학교병원",
                        "경기도 수원시 영통구 월드컵로 164",
                        37.279,
                        127.047
                );

        ChatSessionResponse confirmedSession =
                ChatSessionResponse.builder()
                        .sessionId("session-1")
                        .currentState(
                                ChatState.ORIGIN_CONFIRMATION
                        )
                        .destination(
                                ChatSessionResponse
                                        .PlaceContextResponse
                                        .builder()
                                        .placeId("12345")
                                        .name(
                                                "아주대학교병원"
                                        )
                                        .address(
                                                "경기도 수원시 영통구 월드컵로 164"
                                        )
                                        .latitude(
                                                37.279
                                        )
                                        .longitude(
                                                127.047
                                        )
                                        .build()
                        )
                        .build();

        when(
                chatSessionService
                        .confirmDestination(
                                1L,
                                request
                        )
        ).thenReturn(
                confirmedSession
        );

        ChatSessionResponse response =
                chatService.confirmDestination(
                        1L,
                        request
                );

        assertThat(
                response.getCurrentState()
        ).isEqualTo(
                ChatState.ORIGIN_CONFIRMATION
        );

        assertThat(
                response.getDestination()
        ).isNotNull();

        assertThat(
                response.getDestination()
                        .getPlaceId()
        ).isEqualTo(
                "12345"
        );

        assertThat(
                response.getDestination()
                        .getName()
        ).isEqualTo(
                "아주대학교병원"
        );

        assertThat(
                response.getDestination()
                        .getAddress()
        ).isEqualTo(
                "경기도 수원시 영통구 월드컵로 164"
        );

        assertThat(
                response.getDestination()
                        .getLatitude()
        ).isEqualTo(
                37.279
        );

        assertThat(
                response.getDestination()
                        .getLongitude()
        ).isEqualTo(
                127.047
        );

        verify(
                chatSessionService
        ).confirmDestination(
                1L,
                request
        );
    }
    @Test
    @DisplayName("주변 장소 검색인데 현재 위치가 없으면 위치 요청 응답을 반환한다")
    void searchNearbyPlaceWithoutLocation() {

        ChatSessionResponse session =
                ChatSessionResponse.builder()
                        .sessionId("session-1")
                        .currentState(
                                ChatState.DESTINATION_WAITING
                        )
                        .build();

        AiChatAnalyzeResponse aiResponse =
                AiChatAnalyzeResponse.builder()
                        .intent(
                                ChatIntent.FACILITY
                        )
                        .action(
                                ChatAction.SEARCH_NEARBY_PLACE
                        )
                        .value("이비인후과")
                        .build();

        when(
                chatSessionService
                        .getOrCreateSession(1L)
        ).thenReturn(session);

        when(
                aiChatClient.analyze(any())
        ).thenReturn(aiResponse);

        ChatMessageRequest request =
                new ChatMessageRequest(
                        "근처 이비인후과 찾아줘"
                );

        ChatMessageResponse response =
                chatService.chat(
                        1L,
                        request
                );

        assertThat(
                response.getResponseType()
        ).isEqualTo(
                ChatResponseType.LOCATION_REQUIRED
        );

        assertThat(
                response.getPlaces()
        ).isNull();

        verifyNoInteractions(
                placeSearchService
        );
    }
}