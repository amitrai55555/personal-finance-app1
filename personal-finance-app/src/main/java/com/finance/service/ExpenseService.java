package com.finance.service;

import com.finance.dto.ExpenseRequest;
import com.finance.entity.BankAccount;
import com.finance.entity.Expense;
import com.finance.entity.User;
import com.finance.repository.BankAccountRepository;
import com.finance.repository.ExpenseRepository;
import com.finance.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            BankAccountRepository bankAccountRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    // =====================================================
    // ➕ CREATE EXPENSE (MANUAL / FRONTEND / AA SYNC)
    // =====================================================
    public Expense createExpense(ExpenseRequest request, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔑 Always attach verified primary bank account
        BankAccount bankAccount = bankAccountRepository
                .findPrimaryVerifiedAccountByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("No verified bank account found"));

        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        expense.setIsRecurring(request.getIsRecurring());
        expense.setRecurrenceType(request.getRecurrenceType());
        expense.setNotes(request.getNotes());
        expense.setUser(user);
        expense.setBankAccount(bankAccount);

        // Recurring calculation
        if (Boolean.TRUE.equals(request.getIsRecurring())
                && request.getRecurrenceType() != null) {
            expense.setNextOccurrence(
                    calculateNextOccurrence(request.getDate(),
                            request.getRecurrenceType()));
        }

        return expenseRepository.save(expense);
    }

    // =====================================================
    // 📄 READ OPERATIONS
    // =====================================================
    public List<Expense> getAllExpensesByUserId(Long userId) {
        return expenseRepository.findByUserIdOrderByDateDesc(userId);
    }

    public Optional<Expense> getExpenseById(Long expenseId, Long userId) {
        return expenseRepository.findById(expenseId)
                .filter(e -> e.getUser().getId().equals(userId));
    }

    // =====================================================
    // ✏ UPDATE
    // =====================================================
    public Expense updateExpense(Long expenseId,
                                 ExpenseRequest request,
                                 Long userId) {

        Expense expense = getExpenseById(expenseId, userId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        expense.setIsRecurring(request.getIsRecurring());
        expense.setRecurrenceType(request.getRecurrenceType());
        expense.setNotes(request.getNotes());

        if (Boolean.TRUE.equals(request.getIsRecurring())
                && request.getRecurrenceType() != null) {
            expense.setNextOccurrence(
                    calculateNextOccurrence(request.getDate(),
                            request.getRecurrenceType()));
        } else {
            expense.setNextOccurrence(null);
        }

        return expenseRepository.save(expense);
    }

    // =====================================================
    // ❌ DELETE
    // =====================================================
    public void deleteExpense(Long expenseId, Long userId) {
        Expense expense = getExpenseById(expenseId, userId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        expenseRepository.delete(expense);
    }

    // =====================================================
    // 📊 TOTALS
    // =====================================================
    public BigDecimal getTotalExpenses(Long userId) {
        BigDecimal total =
                expenseRepository.getTotalExpensesByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalExpensesByDateRange(
            Long userId,
            LocalDate startDate,
            LocalDate endDate) {

        BigDecimal total =
                expenseRepository.getTotalExpensesByUserIdAndDateRange(
                        userId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    // =====================================================
    // 🔁 RECURRING HELPER
    // =====================================================
    private LocalDate calculateNextOccurrence(
            LocalDate date,
            Expense.RecurrenceType recurrenceType) {

        return switch (recurrenceType) {
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
            case QUARTERLY -> date.plusMonths(3);
            case YEARLY -> date.plusYears(1);
        };
    }
}
