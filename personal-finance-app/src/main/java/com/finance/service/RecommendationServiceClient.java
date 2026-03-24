package com.finance.service;

import com.finance.dto.PortfolioAllocation;
import com.finance.dto.chat.ChatRequestPayload;
import com.finance.dto.chat.ChatResponse;
import com.finance.dto.coach.CoachAdviceResponse;
import com.finance.dto.coach.FinancialProfileRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RecommendationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(RecommendationServiceClient.class);

    private final RestClient restClient;
    private final String apiKey;

    public RecommendationServiceClient(
            RestClient.Builder builder,
            @Value("${app.recommendation.base-url:http://localhost:8081}") String baseUrl,
            @Value("${app.recommendation.api-key:}") String apiKey
    )

    {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public PortfolioAllocation getInvestmentRecommendations(FinancialProfileRequest request) throws RestClientException {
        return restClient.post()
                .uri("/api/recommendations/investments")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> {
                    if (!apiKey.isEmpty()) {
                        headers.set("X-Internal-Api-Key", apiKey);
                    }
                })
                .body(request)
                .retrieve()
                .body(PortfolioAllocation.class);
    }

    public CoachAdviceResponse getCoachAdvice(FinancialProfileRequest request) throws RestClientException {
        return restClient.post()
                .uri("/api/recommendations/coach")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> {
                    if (!apiKey.isEmpty()) {
                        headers.set("X-Internal-Api-Key", apiKey);
                    }
                })
                .body(request)
                .retrieve()
                .body(CoachAdviceResponse.class);
    }

    public ChatResponse sendChat(ChatRequestPayload request) throws RestClientException {
        return restClient.post()
                .uri("/api/recommendations/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> {
                    if (!apiKey.isEmpty()) {
                        headers.set("X-Internal-Api-Key", apiKey);
                    }
                })
                .body(request)
                .retrieve()
                .body(ChatResponse.class);
    }
}
