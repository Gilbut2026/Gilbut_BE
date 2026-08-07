package com.gilbeot.gilbut.client.ai.dto;

import com.gilbeot.gilbut.domain.chat.ChatState;
import com.gilbeot.gilbut.dto.chat.response.ChatSessionResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiChatAnalyzeRequest {

    private String sessionId;

    private ChatState state;

    private String message;

    private Context context;

    public static AiChatAnalyzeRequest of(
            ChatSessionResponse session,
            String message
    ) {
        return AiChatAnalyzeRequest.builder()
                .sessionId(session.getSessionId())
                .state(session.getCurrentState())
                .message(message)
                .context(
                        Context.builder()
                                .destination(
                                        PlaceContext.from(
                                                session.getDestination()
                                        )
                                )
                                .origin(
                                        PlaceContext.from(
                                                session.getOrigin()
                                        )
                                )
                                .build()
                )
                .build();
    }

    @Getter
    @Builder
    public static class Context {

        private PlaceContext destination;

        private PlaceContext origin;
    }

    @Getter
    @Builder
    public static class PlaceContext {

        private String placeId;

        private String name;

        private String address;

        private Double latitude;

        private Double longitude;

        public static PlaceContext from(
                ChatSessionResponse.PlaceContextResponse place
        ) {
            if (place == null) {
                return null;
            }

            return PlaceContext.builder()
                    .placeId(place.getPlaceId())
                    .name(place.getName())
                    .address(place.getAddress())
                    .latitude(place.getLatitude())
                    .longitude(place.getLongitude())
                    .build();
        }
    }
}