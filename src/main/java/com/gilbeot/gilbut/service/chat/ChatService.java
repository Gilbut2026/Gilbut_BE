package com.gilbeot.gilbut.service.chat;

import com.gilbeot.gilbut.client.ai.AiChatClient;
import com.gilbeot.gilbut.client.ai.dto.AiChatAnalyzeRequest;
import com.gilbeot.gilbut.client.ai.dto.AiChatAnalyzeResponse;
import com.gilbeot.gilbut.domain.chat.ChatIntent;
import com.gilbeot.gilbut.domain.chat.ChatResponseType;
import com.gilbeot.gilbut.domain.chat.ChatState;
import com.gilbeot.gilbut.dto.chat.request.OriginConfirmationRequest;
import com.gilbeot.gilbut.dto.chat.request.PlaceConfirmationRequest;
import com.gilbeot.gilbut.dto.chat.request.ChatMessageRequest;
import com.gilbeot.gilbut.dto.chat.request.DepartureTimeConfirmationRequest;
import com.gilbeot.gilbut.dto.chat.response.ChatMessageResponse;
import com.gilbeot.gilbut.dto.chat.response.ChatSessionResponse;
import com.gilbeot.gilbut.dto.place.request.PlaceSearchRequest;
import com.gilbeot.gilbut.dto.place.response.PlaceItemResponse;
import com.gilbeot.gilbut.dto.place.response.PlaceSearchResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.service.place.PlaceSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String PLACE_SEARCH_SIZE = "5";
    private static final String NEARBY_SEARCH_RADIUS_KM = "5";

    private final ChatSessionService chatSessionService;

    private final AiChatClient aiChatClient;

    private final PlaceSearchService placeSearchService;

    public ChatMessageResponse chat(
            Long userId,
            ChatMessageRequest request
    ) {
        ChatSessionResponse session =
                chatSessionService
                        .getOrCreateSession(userId);

        AiChatAnalyzeRequest aiRequest =
                AiChatAnalyzeRequest.of(
                        session,
                        request.getMessage().trim()
                );

        AiChatAnalyzeResponse aiResponse =
                aiChatClient.analyze(aiRequest);

        validateAiResponse(aiResponse);

        return switch (aiResponse.getAction()) {
            case SEARCH_DESTINATION ->
                    handleSearchDestination(
                            session,
                            aiResponse
                    );

            case SEARCH_NEARBY_PLACE ->
                    handleSearchNearbyPlace(
                            session,
                            request,
                            aiResponse
                    );

            case OUT_OF_SCOPE ->
                    handleOutOfScope(session);
        };
    }

    public ChatSessionResponse confirmDestination(
            Long userId,
            PlaceConfirmationRequest request
    ) {
        return chatSessionService.confirmDestination(
                userId,
                request
        );
    }

    public ChatSessionResponse confirmOrigin(
            Long userId,
            OriginConfirmationRequest request
    ) {
        return chatSessionService.confirmOrigin(
                userId,
                request
        );
    }

    private ChatMessageResponse handleSearchDestination(
            ChatSessionResponse session,
            AiChatAnalyzeResponse aiResponse
    ) {
        if (session.getCurrentState()
                != ChatState.DESTINATION_WAITING) {

            throw new CustomException(
                    ErrorCode.CHAT_STATE_CONFLICT
            );
        }

        if (aiResponse.getIntent()
                != ChatIntent.DESTINATION) {

            throw new CustomException(
                    ErrorCode.AI_CHAT_RESPONSE_INVALID
            );
        }

        String keyword =
                aiResponse.getValue();

        if (!StringUtils.hasText(keyword)) {
            throw new CustomException(
                    ErrorCode.AI_CHAT_RESPONSE_INVALID
            );
        }

        PlaceSearchRequest placeRequest =
                new PlaceSearchRequest();

        placeRequest.setKeyword(
                keyword.trim()
        );

        placeRequest.setSize(
                PLACE_SEARCH_SIZE
        );

        PlaceSearchResponse searchResponse =
                placeSearchService.search(
                        placeRequest
                );

        List<PlaceItemResponse> places =
                searchResponse.getPlaces();

        String message =
                places == null || places.isEmpty()
                        ? "검색 결과를 찾지 못했어요. 목적지를 조금 더 구체적으로 말씀해 주세요."
                        : "검색 결과에서 목적지를 선택해 주세요.";

        return ChatMessageResponse.builder()
                .sessionId(
                        session.getSessionId()
                )
                .currentState(
                        session.getCurrentState()
                )
                .responseType(
                        ChatResponseType.PLACE_CANDIDATES
                )
                .message(message)
                .places(places)
                .build();
    }

    private ChatMessageResponse handleSearchNearbyPlace(
            ChatSessionResponse session,
            ChatMessageRequest request,
            AiChatAnalyzeResponse aiResponse
    ) {
        if (session.getCurrentState()
                != ChatState.DESTINATION_WAITING) {

            throw new CustomException(
                    ErrorCode.CHAT_STATE_CONFLICT
            );
        }

        if (aiResponse.getIntent()
                != ChatIntent.FACILITY) {

            throw new CustomException(
                    ErrorCode.AI_CHAT_RESPONSE_INVALID
            );
        }

        String keyword = aiResponse.getValue();

        if (!StringUtils.hasText(keyword)) {
            throw new CustomException(
                    ErrorCode.AI_CHAT_RESPONSE_INVALID
            );
        }

        Double latitude = request.getLatitude();
        Double longitude = request.getLongitude();

        if (latitude == null && longitude == null) {
            return ChatMessageResponse.builder()
                    .sessionId(session.getSessionId())
                    .currentState(session.getCurrentState())
                    .responseType(ChatResponseType.LOCATION_REQUIRED)
                    .message("근처 장소를 찾으려면 현재 위치가 필요해요. 위치 사용을 켜 주세요.")
                    .build();
        }

        if (latitude == null || longitude == null) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        PlaceSearchRequest placeRequest =
                new PlaceSearchRequest();

        placeRequest.setKeyword(keyword.trim());
        placeRequest.setLat(String.valueOf(latitude));
        placeRequest.setLon(String.valueOf(longitude));
        placeRequest.setRadiusKm(NEARBY_SEARCH_RADIUS_KM);
        placeRequest.setSize(PLACE_SEARCH_SIZE);

        PlaceSearchResponse searchResponse =
                placeSearchService.search(placeRequest);

        List<PlaceItemResponse> places =
                searchResponse.getPlaces();

        String message =
                places == null || places.isEmpty()
                        ? "현재 위치 근처에서 검색 결과를 찾지 못했어요. 다른 검색어로 말씀해 주세요."
                        : "현재 위치 근처에서 찾았어요. 가실 곳을 선택해 주세요.";

        return ChatMessageResponse.builder()
                .sessionId(session.getSessionId())
                .currentState(session.getCurrentState())
                .responseType(ChatResponseType.PLACE_CANDIDATES)
                .message(message)
                .places(places)
                .build();
    }
    private ChatMessageResponse handleOutOfScope(
            ChatSessionResponse session
    ) {
        return ChatMessageResponse.builder()
                .sessionId(
                        session.getSessionId()
                )
                .currentState(
                        session.getCurrentState()
                )
                .responseType(
                        ChatResponseType.TEXT
                )
                .message(
                        "길벗에서는 목적지, 쉼터·화장실, 날씨, DRT·콜택시 관련 요청을 도와드릴 수 있어요."
                )
                .build();
    }

    private void validateAiResponse(
            AiChatAnalyzeResponse response
    ) {
        if (response == null
                || response.getIntent() == null
                || response.getAction() == null) {

            throw new CustomException(
                    ErrorCode.AI_CHAT_RESPONSE_INVALID
            );
        }
    }

    public ChatSessionResponse confirmDepartureTime(
            Long userId,
            DepartureTimeConfirmationRequest request
    ) {
        return chatSessionService
                .confirmDepartureTime(
                        userId,
                        request
                );
    }
}