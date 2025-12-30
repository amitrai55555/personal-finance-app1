package com.finance.service;

import com.finance.dto.ExpenseRequest;
import com.finance.entity.BankAccount;
import com.finance.entity.Expense;
import com.finance.entity.Expense.ExpenseCategory;
import com.finance.entity.User;
import com.finance.repository.BankAccountRepository;
import com.finance.repository.ExpenseRepository;
import com.finance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    // ✅ MANUAL EXPENSE (Controller uses this)
    public Expense createExpense(ExpenseRequest request, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount bankAccount =
                bankAccountRepository
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

        if (Boolean.TRUE.equals(request.getIsRecurring())
                && request.getRecurrenceType() != null) {
            expense.setNextOccurrence(
                    calculateNextOccurrence(request.getDate(), request.getRecurrenceType())
            );
        }

        return expenseRepository.save(expense);
    }

    // ✅ BANK SYNC EXPENSE (AA uses this)
    public Expense createExpenseFromBank(
            ExpenseRequest request,
            Long userId,
            BankAccount bankAccount
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        expense.setUser(user);
        expense.setBankAccount(bankAccount);

        return expenseRepository.save(expense);
    }

    // ---------------- READ APIs ----------------

    public List<Expense> getAllExpensesByUserId(Long userId) {
        return expenseRepository.findByUserIdOrderByDateDesc(userId);
    }

    public Page<Expense> getExpensesByUserId(Long userId, Pageable pageable) {
        return expenseRepository.findByUserIdOrderByDateDesc(userId, pageable);
    }

    public List<Expense> getExpensesByDateRange(
            Long userId, LocalDate startDate, LocalDate endDate) {
        return expenseRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, startDate, endDate);
    }

    public List<Expense> getExpensesByCategory(
            Long userId, ExpenseCategory category) {
        return expenseRepository
                .findByUserIdAndCategoryOrderByDateDesc(userId, category);
    }

    public Optional<Expense> getExpenseById(Long expenseId, Long userId) {
        return expenseRepository.findById(expenseId)
                .filter(e -> e.getUser().getId().equals(userId));
    }

    public Expense updateExpense(
            Long expenseId, ExpenseRequest request, Long userId) {

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
                    calculateNextOccurrence(request.getDate(), request.getRecurrenceType()));
        } else {
            expense.setNextOccurrence(null);
        }

        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long expenseId, Long userId) {
        Expense expense = getExpenseById(expenseId, userId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        expenseRepository.delete(expense);
    }

    // ---------------- DASHBOARD HELPERS ----------------

    public BigDecimal getTotalExpenses(Long userId) {
        BigDecimal total = expenseRepository.getTotalExpensesByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalExpensesByDateRange(
            Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal total =
                expenseRepository.getTotalExpensesByUserIdAndDateRange(
                        userId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    public Map<ExpenseCategory, BigDecimal> getExpensesByCategory(Long userId) {
        List<Object[]> results =
                expenseRepository.getExpensesByCategoryForUser(userId);

        Map<ExpenseCategory, BigDecimal> map = new HashMap<>();
        for (Object[] r : results) {
            map.put((ExpenseCategory) r[0], (BigDecimal) r[1]);
        }
        return map;
    }

    public Map<ExpenseCategory, BigDecimal>
    getExpensesByCategoryAndDateRange(
            Long userId, LocalDate startDate, LocalDate endDate) {

        List<Object[]> results =
                expenseRepository.getExpensesByCategoryForUserAndDateRange(
                        userId, startDate, endDate);

        Map<ExpenseCategory, BigDecimal> map = new HashMap<>();
        for (Object[] r : results) {
            map.put((ExpenseCategory) r[0], (BigDecimal) r[1]);
        }
        return map;
    }

    public List<Expense> getRecentExpenses(Long userId, int limit) {
        return expenseRepository
                .findByUserIdOrderByDateDesc(userId)
                .stream()
                .limit(limit)
                .toList();
    }

    private LocalDate calculateNextOccurrence(
            LocalDate date, Expense.RecurrenceType recurrenceType) {

        return switch (recurrenceType) {
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
            case QUARTERLY -> date.plusMonths(3);
            case YEARLY -> date.plusYears(1);
        };
    }
}
