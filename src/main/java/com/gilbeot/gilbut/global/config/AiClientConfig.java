package com.gilbeot.gilbut.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AiClientConfig {

    @Bean
    public RestTemplate aiRestTemplate(
            @Value("${ai.connect-timeout-ms}")
            int connectTimeoutMs,
            @Value("${ai.read-timeout-ms}")
            int readTimeoutMs
    ) {
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        return new RestTemplate(factory);
    }
}