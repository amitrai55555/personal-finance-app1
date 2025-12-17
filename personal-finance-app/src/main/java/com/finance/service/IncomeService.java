
package com.finance.service;
import com.finance.dto.IncomeRequest;
import com.finance.entity.Income;
import com.finance.entity.Income.IncomeCategory;
import com.finance.entity.User;
import com.finance.repository.IncomeRepository;
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
public class IncomeService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private UserRepository userRepository;

    public Income createIncome(IncomeRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Income income = new Income();
        income.setDescription(request.getDescription());
        income.setAmount(request.getAmount());
        income.setCategory(request.getCategory());
        income.setDate(request.getDate());
        income.setIsRecurring(request.getIsRecurring());
        income.setRecurrenceType(request.getRecurrenceType());
        income.setNotes(request.getNotes());
        income.setUser(user);

        // Calculate next occurrence for recurring income
        if (Boolean.TRUE.equals(request.getIsRecurring()) && request.getRecurrenceType() != null) {
            income.setNextOccurrence(calculateNextOccurrence(request.getDate(), request.getRecurrenceType()));
        }

        return incomeRepository.save(income);
    }

    public List<Income> getAllIncomesByUserId(Long userId) {
        return incomeRepository.findByUserIdOrderByDateDesc(userId);
    }

    public Page<Income> getIncomesByUserId(Long userId, Pageable pageable) {
        return incomeRepository.findByUserIdOrderByDateDesc(userId, pageable);
    }

    public List<Income> getIncomesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return incomeRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, startDate, endDate);
    }

    public List<Income> getIncomesByCategory(Long userId, IncomeCategory category) {
        return incomeRepository.findByUserIdAndCategoryOrderByDateDesc(userId, category);
    }

    public Optional<Income> getIncomeById(Long incomeId, Long userId) {
        return incomeRepository.findById(incomeId)
                .filter(income -> income.getUser().getId().equals(userId));
    }

    public Income updateIncome(Long incomeId, IncomeRequest request, Long userId) {
        Income income = getIncomeById(incomeId, userId)
                .orElseThrow(() -> new RuntimeException("Income not found"));

        income.setDescription(request.getDescription());
        income.setAmount(request.getAmount());
        income.setCategory(request.getCategory());
        income.setDate(request.getDate());
        income.setIsRecurring(request.getIsRecurring());
        income.setRecurrenceType(request.getRecurrenceType());
        income.setNotes(request.getNotes());

        // Update next occurrence for recurring income
        if (Boolean.TRUE.equals(request.getIsRecurring()) && request.getRecurrenceType() != null) {
            income.setNextOccurrence(calculateNextOccurrence(request.getDate(), request.getRecurrenceType()));
        } else {
            income.setNextOccurrence(null);
        }

        return incomeRepository.save(income);
    }

    public void deleteIncome(Long incomeId, Long userId) {
        Income income = getIncomeById(incomeId, userId)
                .orElseThrow(() -> new RuntimeException("Income not found"));
        incomeRepository.delete(income);
    }

    public BigDecimal getTotalIncome(Long userId) {
        BigDecimal total = incomeRepository.getTotalIncomeByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalIncomeByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = incomeRepository.getTotalIncomeByUserIdAndDateRange(userId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    public Map<IncomeCategory, BigDecimal> getIncomeByCategory(Long userId) {
        List<Object[]> results = incomeRepository.getIncomeByCategoryForUser(userId);
        Map<IncomeCategory, BigDecimal> categoryMap = new HashMap<>();

        for (Object[] result : results) {
            IncomeCategory category = (IncomeCategory) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            categoryMap.put(category, amount);
        }

        return categoryMap;
    }

    public Map<IncomeCategory, BigDecimal> getIncomeByCategoryAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = incomeRepository.getIncomeByCategoryForUserAndDateRange(userId, startDate, endDate);
        Map<IncomeCategory, BigDecimal> categoryMap = new HashMap<>();

        for (Object[] result : results) {
            IncomeCategory category = (IncomeCategory) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            categoryMap.put(category, amount);
        }

        return categoryMap;
    }

    public List<Income> getRecentIncomes(Long userId, int limit) {
        List<Income> allIncomes = incomeRepository.findByUserIdOrderByDateDesc(userId);
        return allIncomes.stream().limit(limit).toList();
    }

    private LocalDate calculateNextOccurrence(LocalDate date, Income.RecurrenceType recurrenceType) {
        switch (recurrenceType) {
            case WEEKLY:
                return date.plusWeeks(1);
            case MONTHLY:
                return date.plusMonths(1);
            case QUARTERLY:
                return date.plusMonths(3);
            case YEARLY:
                return date.plusYears(1);
            default:
                return null;
        }
    }
}
