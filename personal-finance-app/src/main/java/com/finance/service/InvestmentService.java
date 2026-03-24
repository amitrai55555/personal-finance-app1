package com.finance.service;

import com.finance.dto.PortfolioAllocation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Generates portfolio recommendations for a user.
 * Now purely local: derives allocations from the user's financial insights
 * without calling external AI or recommendation services.
 */
@Service
public class InvestmentService {

    private final DashboardService dashboardService;

    public InvestmentService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Generate a local allocation envelope using the user's real financial data.
     * No external calls are made and no stock picks are returned.
     */
    public PortfolioAllocation generatePortfolioRecommendation(Long userId, String riskProfile) {
        String resolvedRisk = riskProfile == null ? determineRiskProfile(userId) : riskProfile;

        // Bare allocation based on user's real data (no hardcoded or AI picks)
        Map<String, Object> insights = dashboardService.getFinancialInsights(userId);
        BigDecimal investmentCapacity = (BigDecimal) insights.getOrDefault("investmentCapacity", BigDecimal.ZERO);

        // Use the user's actual investment capacity (even if zero)
        PortfolioAllocation allocation = new PortfolioAllocation(investmentCapacity, resolvedRisk);
        // Explicitly return no recommendations to avoid stock suggestions
        allocation.setRecommendations(java.util.List.of());
        return allocation;
    }

    public BigDecimal calculateInvestmentCapacity(Long userId) {
        Map<String, Object> insights = dashboardService.getFinancialInsights(userId);
        BigDecimal monthlySavings = (BigDecimal) insights.get("monthlySavings");
        // 20% of monthly savings for investments
        return monthlySavings.multiply(BigDecimal.valueOf(0.20));
    }

    /**
     * Determine risk profile from user's actual savings data.
     */
    public String determineRiskProfile(Long userId) {
        Map<String, Object> insights = dashboardService.getFinancialInsights(userId);
        BigDecimal monthlySavings = (BigDecimal) insights.get("monthlySavings");
        Double savingsRate = (Double) insights.get("savingsRate");

        if (savingsRate == null)
            savingsRate = 0.0;
        if (monthlySavings == null)
            monthlySavings = BigDecimal.ZERO;

        if (savingsRate > 30.0 && monthlySavings.compareTo(BigDecimal.valueOf(2000)) > 0) {
            return "AGGRESSIVE";
        } else if (savingsRate > 15.0 && monthlySavings.compareTo(BigDecimal.valueOf(500)) > 0) {
            return "MODERATE";
        } else {
            return "CONSERVATIVE";
        }
    }
}
