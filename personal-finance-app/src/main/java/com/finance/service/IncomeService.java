package com.finance.service;

import com.finance.dto.IncomeRequest;
import com.finance.entity.Income;
import com.finance.entity.User;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.IncomeRepository;
import com.finance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IncomeService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private UserRepository userRepository;

    // ================= CREATE =================
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
        income.setRecurring(request.isRecurring());
        income.setUser(user);

        return incomeRepository.save(income);
    }

    // ================= READ =================
    public List<Income> getAllIncomesByUserId(Long userId) {
        return incomeRepository.findByUserIdOrderByDateDesc(userId);
    }

    public Page<Income> getIncomesByUserId(Long userId, Pageable pageable) {
        return incomeRepository.findByUserId(userId, pageable);
    }

    public java.util.Optional<Income> getIncomeById(Long incomeId, Long userId) {
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
        income.setRecurring(request.isRecurring());

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
        return incomeRepository.getTotalIncomeByUserId(userId)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getTotalIncomeByDateRange(
            Long userId, LocalDate startDate, LocalDate endDate) {

        return incomeRepository.getTotalIncomeByUserIdAndDateRange(
                        userId, startDate, endDate)
                .orElse(BigDecimal.ZERO);
    }

    // ================= CATEGORY =================
    public Map<Income.IncomeCategory, BigDecimal> getIncomeByCategory(Long userId) {

        List<Object[]> results =
                incomeRepository.getIncomeGroupedByCategory(userId);

        return results.stream()
                .collect(Collectors.toMap(
                        r -> (Income.IncomeCategory) r[0],
                        r -> (BigDecimal) r[1]
                ));
    }

    public Map<Income.IncomeCategory, BigDecimal> getIncomeByCategoryAndDateRange(
            Long userId, LocalDate startDate, LocalDate endDate) {

        List<Object[]> results =
                incomeRepository.getIncomeGroupedByCategoryAndDateRange(
                        userId, startDate, endDate);

        return results.stream()
                .collect(Collectors.toMap(
                        r -> (Income.IncomeCategory) r[0],
                        r -> (BigDecimal) r[1]
                ));
    }

    // ================= RECENT =================
    public List<Income> getRecentIncomes(Long userId, int limit) {
        return incomeRepository.findTopNByUserIdOrderByDateDesc(userId, limit);
    }
}
