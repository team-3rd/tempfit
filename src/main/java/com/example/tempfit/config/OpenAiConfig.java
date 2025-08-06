package com.example.tempfit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenAiConfig {
    @Value("${spring.ai.openai.api-key}")
    private String openAiKey;
    @Value("${spring.ai.openai.api-base-url}")
    private String openAiBaseUrl;
    @Bean
    public RestTemplate openAiRestTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            // 인증
            request.getHeaders().add("Authorization", "Bearer " + openAiKey);
            // JSON 요청임을 명시
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}