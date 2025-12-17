package com.finance.dto;

import com.finance.entity.Income.IncomeCategory;
import com.finance.entity.Income.RecurrenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class IncomeRequest {
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Category is required")
    private IncomeCategory category;
    
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    private Boolean isRecurring = false;
    
    private RecurrenceType recurrenceType;
    
    private String notes;
    @NotNull
    private Long bankAccountId;


    // Constructors
    public IncomeRequest() {}
    
    public IncomeRequest(String description, BigDecimal amount, IncomeCategory category, LocalDate date) {
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }
    
    // Getters and Setters
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public IncomeCategory getCategory() {
        return category;
    }
    
    public void setCategory(IncomeCategory category) {
        this.category = category;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public Boolean getIsRecurring() {
        return isRecurring;
    }
    
    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }
    
    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }
    
    public void setRecurrenceType(RecurrenceType recurrenceType) {
        this.recurrenceType = recurrenceType;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }


}
