package com.finance.service;

import com.finance.dto.ExpenseRequest;
import com.finance.entity.BankAccount;
import com.finance.entity.Expense;
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


    public Expense createExpense(ExpenseRequest request, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount bankAccount;
        if (request.getBankAccountId() != null) {
            bankAccount = bankAccountRepository
                    .findByIdAndUser(request.getBankAccountId(), user)
                    .orElseThrow(() -> new RuntimeException("Bank account not found for this user"));
            if (!bankAccount.isVerified()) {
                throw new RuntimeException("Selected bank account is not verified");
            }
        } else {
            bankAccount = bankAccountRepository
                    .findPrimaryVerifiedAccountByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("No verified bank account found"));
        }

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

        if (Boolean.TRUE.equals(request.getIsRecurring()) &&
                request.getRecurrenceType() != null) {
            expense.setNextOccurrence(
                    calculateNextOccurrence(request.getDate(), request.getRecurrenceType())
            );
        }

        return expenseRepository.save(expense);
    }


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


    public List<Expense> getAllExpensesByUserId(Long userId) {
        return expenseRepository.findByUserIdOrderByDateDesc(userId);
    }

    public Page<Expense> getExpensesByUserId(Long userId, Pageable pageable) {
        return expenseRepository.findByUserIdOrderByDateDesc(userId, pageable);
    }

    public Optional<Expense> getExpenseById(Long expenseId, Long userId) {
        return expenseRepository.findById(expenseId)
                .filter(e -> e.getUser().getId().equals(userId));
    }


    public Expense updateExpense(Long expenseId, ExpenseRequest request, Long userId) {

        Expense expense = getExpenseById(expenseId, userId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        expense.setIsRecurring(request.getIsRecurring());
        expense.setRecurrenceType(request.getRecurrenceType());
        expense.setNotes(request.getNotes());

        if (Boolean.TRUE.equals(request.getIsRecurring()) &&
                request.getRecurrenceType() != null) {
            expense.setNextOccurrence(
                    calculateNextOccurrence(request.getDate(), request.getRecurrenceType())
            );
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

    public BigDecimal getTotalExpenses(Long userId) {
        BigDecimal total = expenseRepository.getTotalExpensesByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalExpensesByDateRange(
            Long userId, LocalDate start, LocalDate end) {

        BigDecimal total =
                expenseRepository.getTotalExpensesByUserIdAndDateRange(userId, start, end);
        return total != null ? total : BigDecimal.ZERO;
    }


    public Map<Expense.ExpenseCategory, BigDecimal>
    getExpensesByCategoryAndDateRange(
            Long userId, LocalDate start, LocalDate end) {

        Map<Expense.ExpenseCategory, BigDecimal> map = new HashMap<>();

        expenseRepository
                .getExpensesByCategoryForUserAndDateRange(userId, start, end)
                .forEach(obj ->
                        map.put(
                                (Expense.ExpenseCategory) obj[0],
                                (BigDecimal) obj[1]
                        )
                );

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
            LocalDate date,
            Expense.RecurrenceType type
    ) {
        return switch (type) {
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
            case QUARTERLY -> date.plusMonths(3);
            case YEARLY -> date.plusYears(1);
        };
    }
}
