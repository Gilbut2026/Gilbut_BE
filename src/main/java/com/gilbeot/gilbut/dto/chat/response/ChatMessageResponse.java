package com.gilbeot.gilbut.dto.chat.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gilbeot.gilbut.domain.chat.ChatResponseType;
import com.gilbeot.gilbut.domain.chat.ChatState;
import com.gilbeot.gilbut.dto.place.response.PlaceItemResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatMessageResponse {

    private String sessionId;

    private ChatState currentState;

    private ChatResponseType responseType;

    private String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PlaceItemResponse> places;
}