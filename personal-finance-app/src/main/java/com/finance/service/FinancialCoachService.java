package com.finance.service;

import com.finance.dto.PortfolioAllocation;
import com.finance.dto.coach.AdviceItem;
import com.finance.dto.coach.CoachAdviceResponse;
import com.finance.dto.coach.FinancialProfileRequest;
import com.finance.dto.coach.GoalSummary;
import com.finance.entity.Goal;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FinancialCoachService {

    private final DashboardService dashboardService;
    private final ExpenseService expenseService;
    private final GoalService goalService;
    private final InvestmentService investmentService;
    private final RecommendationServiceClient recommendationClient;

    public FinancialCoachService(
            DashboardService dashboardService,
            ExpenseService expenseService,
            GoalService goalService,
            InvestmentService investmentService,
            RecommendationServiceClient recommendationClient
    ) {
        this.dashboardService = dashboardService;
        this.expenseService = expenseService;
        this.goalService = goalService;
        this.investmentService = investmentService;
        this.recommendationClient = recommendationClient;
    }

    public CoachAdviceResponse getCoachAdvice(Long userId, String riskProfile) {
        FinancialProfileRequest profile = buildProfile(userId, riskProfile);
        try {
            CoachAdviceResponse response = recommendationClient.getCoachAdvice(profile);
            if (response != null && response.getPortfolio() != null) {
                return response;
            }
        } catch (RestClientException ignored) {
        }

        CoachAdviceResponse fallback = new CoachAdviceResponse();
        PortfolioAllocation portfolio = investmentService.generatePortfolioRecommendation(userId, riskProfile);
        fallback.setPortfolio(portfolio);
        fallback.setSavingsAdvice(List.of(simpleItem(
                "Automate savings",
                "Set an automatic transfer after payday and increase it gradually.",
                "HABIT"
        )));
        fallback.setExpenseAdvice(List.of(simpleItem(
                "Review top categories",
                "Check your top spending categories this month and set a cap for next month.",
                "EXPENSE"
        )));
        fallback.setGoalAdvice(List.of(simpleItem(
                "Fund your goals monthly",
                "For each goal, set a monthly contribution and automate it.",
                "GOAL"
        )));
        return fallback;
    }

    private FinancialProfileRequest buildProfile(Long userId, String riskProfile) {
        Map<String, Object> insights = dashboardService.getFinancialInsights(userId);
        BigDecimal monthlyIncome = (BigDecimal) insights.getOrDefault("monthlyIncome", BigDecimal.ZERO);
        BigDecimal monthlyExpenses = (BigDecimal) insights.getOrDefault("monthlyExpenses", BigDecimal.ZERO);
        BigDecimal monthlySavings = (BigDecimal) insights.getOrDefault("monthlySavings", BigDecimal.ZERO);
        Double savingsRate = (Double) insights.getOrDefault("savingsRate", 0.0);
        BigDecimal investmentCapacity = (BigDecimal) insights.getOrDefault("investmentCapacity", BigDecimal.ZERO);

        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        Map<String, BigDecimal> expensesByCategory = expenseService.getExpensesByCategoryAndDateRange(userId, monthStart, monthEnd)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));

        List<GoalSummary> goals = goalService.getActiveGoalsByUserId(userId)
                .stream()
                .map(FinancialCoachService::toSummary)
                .toList();

        FinancialProfileRequest profile = new FinancialProfileRequest();
        profile.setRiskProfile(riskProfile);
        profile.setMonthlyIncome(monthlyIncome);
        profile.setMonthlyExpenses(monthlyExpenses);
        profile.setMonthlySavings(monthlySavings);
        profile.setSavingsRate(savingsRate);
        profile.setInvestmentCapacity(investmentCapacity);
        profile.setExpensesByCategory(expensesByCategory);
        profile.setGoals(goals);
        return profile;
    }

    private static GoalSummary toSummary(Goal goal) {
        GoalSummary s = new GoalSummary();
        s.setTitle(goal.getTitle());
        s.setTargetAmount(goal.getTargetAmount());
        s.setCurrentAmount(goal.getCurrentAmount());
        s.setTargetDate(goal.getTargetDate());
        s.setPriority(goal.getPriority() == null ? null : goal.getPriority().name());
        return s;
    }

    private static AdviceItem simpleItem(String title, String description, String category) {
        AdviceItem item = new AdviceItem();
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory(category);
        return item;
    }
}
