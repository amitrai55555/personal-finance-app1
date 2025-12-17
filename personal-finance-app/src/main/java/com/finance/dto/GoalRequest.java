package com.finance.dto;

import com.finance.entity.Goal.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GoalRequest {
    
    @NotBlank(message = "Goal title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be positive")
    private BigDecimal targetAmount;
    
    @PositiveOrZero(message = "Current amount cannot be negative")
    private BigDecimal currentAmount = BigDecimal.ZERO;
    
    @NotNull(message = "Target date is required")
    private LocalDate targetDate;
    
    private Priority priority = Priority.MEDIUM;
    
    // Constructors
    public GoalRequest() {}
    
    public GoalRequest(String title, String description, BigDecimal targetAmount, LocalDate targetDate, Priority priority) {
        this.title = title;
        this.description = description;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.priority = priority;
    }
    
    // Getters and Setters
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
    
    public BigDecimal getTargetAmount() {
        return targetAmount;
    }
    
    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }
    
    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }
    
    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }
    
    public LocalDate getTargetDate() {
        return targetDate;
    }
    
    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}
