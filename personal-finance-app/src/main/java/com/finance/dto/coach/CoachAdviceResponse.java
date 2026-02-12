package com.finance.dto.coach;

import com.finance.dto.PortfolioAllocation;

import java.util.List;

public class CoachAdviceResponse {

    private PortfolioAllocation portfolio;
    private List<AdviceItem> savingsAdvice;
    private List<AdviceItem> expenseAdvice;
    private List<AdviceItem> goalAdvice;

    public PortfolioAllocation getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(PortfolioAllocation portfolio) {
        this.portfolio = portfolio;
    }

    public List<AdviceItem> getSavingsAdvice() {
        return savingsAdvice;
    }

    public void setSavingsAdvice(List<AdviceItem> savingsAdvice) {
        this.savingsAdvice = savingsAdvice;
    }

    public List<AdviceItem> getExpenseAdvice() {
        return expenseAdvice;
    }

    public void setExpenseAdvice(List<AdviceItem> expenseAdvice) {
        this.expenseAdvice = expenseAdvice;
    }

    public List<AdviceItem> getGoalAdvice() {
        return goalAdvice;
    }

    public void setGoalAdvice(List<AdviceItem> goalAdvice) {
        this.goalAdvice = goalAdvice;
    }
}

