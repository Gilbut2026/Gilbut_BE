package com.gilbeot.gilbut.client.ai.dto;

import com.gilbeot.gilbut.domain.chat.ChatAction;
import com.gilbeot.gilbut.domain.chat.ChatIntent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatAnalyzeResponse {

    private ChatIntent intent;

    private ChatAction action;

    private String value;

    private String referencePlace;
}