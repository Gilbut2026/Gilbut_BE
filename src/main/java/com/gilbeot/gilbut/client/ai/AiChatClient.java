package com.gilbeot.gilbut.client.ai;

import com.gilbeot.gilbut.client.ai.dto.AiChatAnalyzeRequest;
import com.gilbeot.gilbut.client.ai.dto.AiChatAnalyzeResponse;

public interface AiChatClient {

    AiChatAnalyzeResponse analyze(
            AiChatAnalyzeRequest request
    );
}