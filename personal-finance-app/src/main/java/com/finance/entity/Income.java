package com.finance.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.finance.service.BankAccountService;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "incomes")
@EntityListeners(AuditingEntityListener.class)

public class Income {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Description is required")
    @Column(nullable = false)
    private String description;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncomeCategory category;
    
    @NotNull(message = "Date is required")
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = false;
    
    @Enumerated(EnumType.STRING)
    private RecurrenceType recurrenceType;
    
    @Column(name = "next_occurrence")
    private LocalDate nextOccurrence;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    @JsonIgnore
    private BankAccount bankAccount;

    // Constructors
    public Income() {}
    
    public Income(String description, BigDecimal amount, IncomeCategory category, LocalDate date, User user) {
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.user = user;
    }
    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = (BankAccount) bankAccount;
    }



    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public LocalDate getNextOccurrence() {
        return nextOccurrence;
    }
    
    public void setNextOccurrence(LocalDate nextOccurrence) {
        this.nextOccurrence = nextOccurrence;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public enum IncomeCategory {
        SALARY, FREELANCE, INVESTMENTS, BUSINESS, RENTAL, BONUS, OTHERS, OTHER
    }
    
    public enum RecurrenceType {
        WEEKLY, MONTHLY, QUARTERLY, YEARLY
    }
}
