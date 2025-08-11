package com.example.tempfit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;

@Configuration
public class OpenAIConfig {
    @Value("${spring.ai.openai.api-key}")
    private String openAiKey;
    @Value("${spring.ai.openai.api-base-url}")
    private String openAiBaseUrl;
    // @Bean
    // public RestTemplate openAiRestTemplate() {
    //     RestTemplate rest = new RestTemplate(new SimpleClientHttpRequestFactory());
    //     rest.getInterceptors().add((request, body, execution) -> {
    //         request.getHeaders().add("Authorization", "Bearer " + openAiKey);
    //         request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    //         return execution.execute(request, body);
    //     });
    //     return rest;
    // }
    @Bean
    public WebClient openAiWebClient() {
        // 충분한 버퍼(응답이 길어질 수 있음)
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(8 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(openAiBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .exchangeStrategies(strategies)
                .build();
    }
}