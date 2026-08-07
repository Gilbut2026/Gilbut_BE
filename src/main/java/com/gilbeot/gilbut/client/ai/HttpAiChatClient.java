package com.gilbeot.gilbut.client.ai;

import com.gilbeot.gilbut.client.ai.dto.AiChatAnalyzeRequest;
import com.gilbeot.gilbut.client.ai.dto.AiChatAnalyzeResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpAiChatClient implements AiChatClient {

    private final RestTemplate aiRestTemplate;

    @Value("${ai.chat-url:}")
    private String aiChatUrl;

    @Override
    public AiChatAnalyzeResponse analyze(
            AiChatAnalyzeRequest request
    ) {
        if (!StringUtils.hasText(aiChatUrl)) {
            throw new CustomException(
                    ErrorCode.AI_SERVER_UNAVAILABLE
            );
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            HttpEntity<AiChatAnalyzeRequest> entity =
                    new HttpEntity<>(
                            request,
                            headers
                    );

            ResponseEntity<AiChatAnalyzeResponse> response =
                    aiRestTemplate.postForEntity(
                            aiChatUrl,
                            entity,
                            AiChatAnalyzeResponse.class
                    );

            AiChatAnalyzeResponse body =
                    response.getBody();

            if (!response.getStatusCode().is2xxSuccessful()
                    || body == null) {

                throw new CustomException(
                        ErrorCode.AI_CHAT_RESPONSE_INVALID
                );
            }

            return body;

        } catch (CustomException e) {
            throw e;

        } catch (RestClientException e) {
            log.error(
                    "AI 채팅 서버 호출 실패",
                    e
            );

            throw new CustomException(
                    ErrorCode.AI_CHAT_FAILED
            );
        }
    }
}