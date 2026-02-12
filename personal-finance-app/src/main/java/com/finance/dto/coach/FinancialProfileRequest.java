package com.finance.dto.coach;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class FinancialProfileRequest {

    private String riskProfile;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;
    private BigDecimal monthlySavings;
    private Double savingsRate;
    private BigDecimal investmentCapacity;
    private Map<String, BigDecimal> expensesByCategory;
    private List<GoalSummary> goals;

    public String getRiskProfile() {
        return riskProfile;
    }

    public void setRiskProfile(String riskProfile) {
        this.riskProfile = riskProfile;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public void setMonthlyExpenses(BigDecimal monthlyExpenses) {
        this.monthlyExpenses = monthlyExpenses;
    }

    public BigDecimal getMonthlySavings() {
        return monthlySavings;
    }

    public void setMonthlySavings(BigDecimal monthlySavings) {
        this.monthlySavings = monthlySavings;
    }

    public Double getSavingsRate() {
        return savingsRate;
    }

    public void setSavingsRate(Double savingsRate) {
        this.savingsRate = savingsRate;
    }

    public BigDecimal getInvestmentCapacity() {
        return investmentCapacity;
    }

    public void setInvestmentCapacity(BigDecimal investmentCapacity) {
        this.investmentCapacity = investmentCapacity;
    }

    public Map<String, BigDecimal> getExpensesByCategory() {
        return expensesByCategory;
    }

    public void setExpensesByCategory(Map<String, BigDecimal> expensesByCategory) {
        this.expensesByCategory = expensesByCategory;
    }

    public List<GoalSummary> getGoals() {
        return goals;
    }

    public void setGoals(List<GoalSummary> goals) {
        this.goals = goals;
    }
}

