package com.finance.service;

import com.finance.dto.IncomeRequest;
import com.finance.entity.BankAccount;
import com.finance.entity.Income;
import com.finance.entity.User;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.BankAccountRepository;
import com.finance.repository.IncomeRepository;
import com.finance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IncomeService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    // ================= CREATE (NORMAL) =================
    public Income createIncome(IncomeRequest request, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + userId)
                );

        Income income = new Income();
        income.setAmount(request.getAmount());
        income.setDescription(request.getDescription());
        income.setCategory(request.getCategory());
        income.setDate(request.getDate());
        income.setIsRecurring(request.getIsRecurring());
        income.setRecurrenceType(request.getRecurrenceType());
        income.setNotes(request.getNotes());
        income.setUser(user);

        // Attach primary verified bank account, similar to ExpenseService
        BankAccount bankAccount = bankAccountRepository
                .findPrimaryVerifiedAccountByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No verified bank account found"));
        income.setBankAccount(bankAccount);

        if (Boolean.TRUE.equals(request.getIsRecurring()) &&
                request.getRecurrenceType() != null) {
            income.setNextOccurrence(
                    calculateNextOccurrence(request.getDate(), request.getRecurrenceType())
            );
        }

        return incomeRepository.save(income);
    }

    // ================= CREATE (FROM BANK / AA) =================
    public Income createIncomeFromBank(
            IncomeRequest request,
            Long userId,
            BankAccount bankAccount
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Income income = new Income();
        income.setAmount(request.getAmount());
        income.setDescription(request.getDescription());
        income.setCategory(request.getCategory());
        income.setDate(request.getDate());
        income.setUser(user);
        income.setBankAccount(bankAccount);

        return incomeRepository.save(income);
    }

    // ================= READ =================
    public List<Income> getAllIncomesByUserId(Long userId) {
        return incomeRepository.findByUserIdOrderByDateDesc(userId);
    }

    public Page<Income> getIncomesByUserId(Long userId, Pageable pageable) {
        return incomeRepository.findByUserId(userId, pageable);
    }

    public Optional<Income> getIncomeById(Long incomeId, Long userId) {
        return incomeRepository.findByIdAndUserId(incomeId, userId);
    }

    // ================= UPDATE =================
    public Income updateIncome(Long incomeId, IncomeRequest request, Long userId) {

        Income income = incomeRepository.findByIdAndUserId(incomeId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Income not found")
                );

        income.setAmount(request.getAmount());
        income.setDescription(request.getDescription());
        income.setCategory(request.getCategory());
        income.setDate(request.getDate());
        income.setIsRecurring(request.getIsRecurring());
        income.setRecurrenceType(request.getRecurrenceType());
        income.setNotes(request.getNotes());

        if (Boolean.TRUE.equals(request.getIsRecurring()) &&
                request.getRecurrenceType() != null) {
            income.setNextOccurrence(
                    calculateNextOccurrence(request.getDate(), request.getRecurrenceType())
            );
        } else {
            income.setNextOccurrence(null);
        }

        return incomeRepository.save(income);
    }

    // ================= DELETE =================
    public void deleteIncome(Long incomeId, Long userId) {

        Income income = incomeRepository.findByIdAndUserId(incomeId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Income not found")
                );

        incomeRepository.delete(income);
    }

    // ================= TOTAL =================
    public BigDecimal getTotalIncome(Long userId) {
        BigDecimal total = incomeRepository.getTotalIncomeByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalIncomeByDateRange(
            Long userId, LocalDate startDate, LocalDate endDate) {

        BigDecimal total = incomeRepository.getTotalIncomeByUserIdAndDateRange(
                userId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    // ================= CATEGORY =================
    public Map<Income.IncomeCategory, BigDecimal> getIncomeByCategory(Long userId) {

        Map<Income.IncomeCategory, BigDecimal> map = new HashMap<>();

        incomeRepository
                .getIncomeByCategoryForUser(userId)
                .forEach(obj ->
                        map.put(
                                (Income.IncomeCategory) obj[0],
                                (BigDecimal) obj[1]
                        )
                );

        return map;
    }

    public Map<Income.IncomeCategory, BigDecimal> getIncomeByCategoryAndDateRange(
            Long userId, LocalDate startDate, LocalDate endDate) {

        Map<Income.IncomeCategory, BigDecimal> map = new HashMap<>();

        incomeRepository
                .getIncomeByCategoryForUserAndDateRange(userId, startDate, endDate)
                .forEach(obj ->
                        map.put(
                                (Income.IncomeCategory) obj[0],
                                (BigDecimal) obj[1]
                        )
                );

        return map;
    }

    // ================= RECENT =================
    public List<Income> getRecentIncomes(Long userId, int limit) {
        return incomeRepository
                .findByUserIdOrderByDateDesc(userId)
                .stream()
                .limit(limit)
                .toList();
    }

    // ================= UTIL =================
    private LocalDate calculateNextOccurrence(
            LocalDate date,
            Income.RecurrenceType type
    ) {
        return switch (type) {
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
            case QUARTERLY -> date.plusMonths(3);
            case YEARLY -> date.plusYears(1);
        };
    }
}
