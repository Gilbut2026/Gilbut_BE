package com.gilbeot.gilbut.client.ai;

import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringRequest;
import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringResponse;
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
public class HttpAiRouteScoringClient
        implements AiRouteScoringClient {

    private final RestTemplate aiRestTemplate;

    @Value("${ai.scoring-url:}")
    private String aiScoringUrl;

    @Override
    public AiRouteScoringResponse score(
            AiRouteScoringRequest request
    ) {
        if (!StringUtils.hasText(aiScoringUrl)) {
            throw new CustomException(
                    ErrorCode.AI_SERVER_UNAVAILABLE
            );
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            HttpEntity<AiRouteScoringRequest> entity =
                    new HttpEntity<>(
                            request,
                            headers
                    );

            ResponseEntity<AiRouteScoringResponse> response =
                    aiRestTemplate.postForEntity(
                            aiScoringUrl,
                            entity,
                            AiRouteScoringResponse.class
                    );

            AiRouteScoringResponse body =
                    response.getBody();

            if (!response.getStatusCode().is2xxSuccessful()
                    || body == null) {
                throw new CustomException(
                        ErrorCode.AI_ROUTE_SCORING_RESPONSE_INVALID
                );
            }

            return body;

        } catch (CustomException e) {
            throw e;

        } catch (RestClientException e) {
            log.error(
                    "AI 경로 스코어링 서버 호출 실패",
                    e
            );

            throw new CustomException(
                    ErrorCode.AI_ROUTE_SCORING_FAILED
            );
        }
    }
}