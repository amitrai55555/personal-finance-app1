package com.finance.dto.coach;

import java.math.BigDecimal;
import java.util.List;

public class AdviceItem {

    private String title;
    private String description;
    private String category;
    private BigDecimal suggestedMonthlyAmount;
    private BigDecimal potentialMonthlySavings;
    private List<String> actionSteps;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getSuggestedMonthlyAmount() {
        return suggestedMonthlyAmount;
    }

    public void setSuggestedMonthlyAmount(BigDecimal suggestedMonthlyAmount) {
        this.suggestedMonthlyAmount = suggestedMonthlyAmount;
    }

    public BigDecimal getPotentialMonthlySavings() {
        return potentialMonthlySavings;
    }

    public void setPotentialMonthlySavings(BigDecimal potentialMonthlySavings) {
        this.potentialMonthlySavings = potentialMonthlySavings;
    }

    public List<String> getActionSteps() {
        return actionSteps;
    }

    public void setActionSteps(List<String> actionSteps) {
        this.actionSteps = actionSteps;
    }
}

