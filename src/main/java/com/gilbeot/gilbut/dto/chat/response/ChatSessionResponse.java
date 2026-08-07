package com.gilbeot.gilbut.dto.chat.response;

import com.gilbeot.gilbut.domain.chat.ChatSession;
import com.gilbeot.gilbut.domain.chat.ChatState;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatSessionResponse {

    private String sessionId;

    private ChatState currentState;

    private PlaceContextResponse destination;

    private PlaceContextResponse origin;

    private String selectedRouteId;

    private String activeRequestId;

    public static ChatSessionResponse from(ChatSession session) {
        return ChatSessionResponse.builder()
                .sessionId(session.getSessionId())
                .currentState(session.getCurrentState())
                .destination(createDestination(session))
                .origin(createOrigin(session))
                .selectedRouteId(session.getSelectedRouteId())
                .activeRequestId(session.getActiveRequestId())
                .build();
    }

    private static PlaceContextResponse createDestination(
            ChatSession session
    ) {
        if (session.getDestinationName() == null
                && session.getDestinationLatitude() == null
                && session.getDestinationLongitude() == null) {
            return null;
        }

        return PlaceContextResponse.builder()
                .placeId(session.getDestinationPlaceId())
                .name(session.getDestinationName())
                .address(session.getDestinationAddress())
                .latitude(session.getDestinationLatitude())
                .longitude(session.getDestinationLongitude())
                .build();
    }

    private static PlaceContextResponse createOrigin(
            ChatSession session
    ) {
        if (session.getOriginName() == null
                && session.getOriginLatitude() == null
                && session.getOriginLongitude() == null) {
            return null;
        }

        return PlaceContextResponse.builder()
                .placeId(session.getOriginPlaceId())
                .name(session.getOriginName())
                .address(session.getOriginAddress())
                .latitude(session.getOriginLatitude())
                .longitude(session.getOriginLongitude())
                .build();
    }

    @Getter
    @Builder
    public static class PlaceContextResponse {

        private String placeId;

        private String name;

        private String address;

        private Double latitude;

        private Double longitude;
    }
}