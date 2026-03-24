package com.finance.service;

import com.finance.dto.chat.ChatMessageRequest;
import com.finance.dto.chat.ChatRequestPayload;
import com.finance.dto.chat.ChatResponse;
import com.finance.dto.coach.AdviceItem;
import com.finance.dto.coach.FinancialProfileRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

@Service
public class AiChatService {

    private final FinancialCoachService financialCoachService;
    private final InvestmentService investmentService;
    private final RecommendationServiceClient recommendationServiceClient;

    public AiChatService(FinancialCoachService financialCoachService,
                         InvestmentService investmentService,
                         RecommendationServiceClient recommendationServiceClient) {
        this.financialCoachService = financialCoachService;
        this.investmentService = investmentService;
        this.recommendationServiceClient = recommendationServiceClient;
    }

    public ChatResponse sendMessage(Long userId, ChatMessageRequest messageRequest) {
        String resolvedRiskProfile = resolveRiskProfile(userId, messageRequest.getRiskProfile());
        FinancialProfileRequest profile = financialCoachService.buildProfile(userId, resolvedRiskProfile);

        ChatRequestPayload payload = new ChatRequestPayload();
        payload.setMessage(messageRequest.getMessage());
        payload.setTopic(messageRequest.getTopic());
        payload.setRiskProfile(resolvedRiskProfile);
        payload.setConversationId(messageRequest.getConversationId());
        payload.setProfile(profile);

        try {
            ChatResponse response = recommendationServiceClient.sendChat(payload);
            if (response != null && response.getReply() != null) {
                return response;
            }
        } catch (RestClientException ignored) {
        }

        return fallbackResponse(payload);
    }

    private String resolveRiskProfile(Long userId, String requested) {
        if (requested != null && !requested.isBlank()) {
            return requested.toUpperCase();
        }
        return investmentService.determineRiskProfile(userId);
    }

    private ChatResponse fallbackResponse(ChatRequestPayload payload) {
        ChatResponse response = new ChatResponse();
        response.setConversationId(Optional.ofNullable(payload.getConversationId()).orElse("local-fallback"));

        String topic = Optional.ofNullable(payload.getTopic()).orElse("general");
        String reply = switch (topic.toLowerCase()) {
            case "savings" -> "Focus on automating a monthly transfer right after payday. Gradually raise it until you reach a 20% savings rate.";
            case "investing" -> "Based on your " + payload.getRiskProfile() + " profile, stay diversified and contribute monthly. Aim for the target mix shown in your dashboard.";
            case "debt" -> "List debts by rate, pay the minimums on all, and attack the highest-rate balance with every extra rupee.";
            default -> "Keep your spending aligned with your goals. Review top categories this month, set caps, and automate contributions toward your priorities.";
        };

        response.setReply(reply);
        response.setSuggestions(List.of(
                simpleAdvice("Check spending", "Review this month's top 3 categories and set a cap for next month.", "EXPENSE"),
                simpleAdvice("Automate savings", "Schedule a transfer after payday toward your highest-priority goal.", "SAVINGS")
        ));
        return response;
    }

    private AdviceItem simpleAdvice(String title, String description, String category) {
        AdviceItem item = new AdviceItem();
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory(category);
        return item;
    }
}
