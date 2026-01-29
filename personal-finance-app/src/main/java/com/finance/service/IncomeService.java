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

@Service
public class IncomeService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    // ================= CREATE =================
    public Income createIncome(IncomeRequest request, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + userId)
                );

        BankAccount bankAccount;
        if (request.getBankAccountId() != null) {
            bankAccount = bankAccountRepository
                    .findByIdAndUser(request.getBankAccountId(), user)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Bank account not found for this user")
                    );
            if (!bankAccount.isVerified()) {
                throw new ResourceNotFoundException("Selected bank account is not verified");
            }
        } else {
            bankAccount = bankAccountRepository
                    .findPrimaryVerifiedAccountByUserId(userId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("No verified primary bank account found")
                    );
        }

        Income income = new Income();
        income.setAmount(request.getAmount());
        income.setDescription(request.getDescription());
        income.setCategory(request.getCategory());
        income.setDate(request.getDate());
        income.setIsRecurring(request.getIsRecurring());
        income.setRecurrenceType(request.getRecurrenceType());
        income.setNotes(request.getNotes());
        income.setUser(user);
        income.setBankAccount(bankAccount);

        handleRecurring(income, request);

        return incomeRepository.save(income);
    }

    // ================= CREATE FROM BANK =================
    public Income createIncomeFromBank(
            IncomeRequest request,
            Long userId,
            BankAccount bankAccount
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + userId)
                );

        Income income = new Income();
        income.setAmount(request.getAmount());
        income.setDescription(request.getDescription());
        income.setCategory(request.getCategory());
        income.setDate(request.getDate());
        income.setUser(user);
        income.setBankAccount(bankAccount);

        handleRecurring(income, request);

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
                        new ResourceNotFoundException("Income not found or not authorized")
                );

        income.setAmount(request.getAmount());
        income.setDescription(request.getDescription());
        income.setCategory(request.getCategory());
        income.setDate(request.getDate());
        income.setIsRecurring(request.getIsRecurring());
        income.setRecurrenceType(request.getRecurrenceType());
        income.setNotes(request.getNotes());

        handleRecurring(income, request);

        return incomeRepository.save(income);
    }


    public void deleteIncome(Long incomeId, Long userId) {

        Income income = incomeRepository.findByIdAndUserId(incomeId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Income not found or not authorized")
                );

        incomeRepository.delete(income);
    }


    public BigDecimal getTotalIncome(Long userId) {
        return Optional.ofNullable(
                incomeRepository.getTotalIncomeByUserId(userId)
        ).orElse(BigDecimal.ZERO);
    }

    public BigDecimal getTotalIncomeByDateRange(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return Optional.ofNullable(
                incomeRepository.getTotalIncomeByUserIdAndDateRange(
                        userId, startDate, endDate)
        ).orElse(BigDecimal.ZERO);
    }


    public Map<Income.IncomeCategory, BigDecimal> getIncomeByCategory(Long userId) {

        Map<Income.IncomeCategory, BigDecimal> map = new HashMap<>();

        incomeRepository.getIncomeByCategoryForUser(userId)
                .forEach(obj ->
                        map.put(
                                (Income.IncomeCategory) obj[0],
                                (BigDecimal) obj[1]
                        )
                );

        return map;
    }

    public Map<Income.IncomeCategory, BigDecimal> getIncomeByCategoryAndDateRange(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    ) {

        Map<Income.IncomeCategory, BigDecimal> map = new HashMap<>();

        incomeRepository
                .getIncomeByCategoryForUserAndDateRange(
                        userId, startDate, endDate)
                .forEach(obj ->
                        map.put(
                                (Income.IncomeCategory) obj[0],
                                (BigDecimal) obj[1]
                        )
                );

        return map;
    }


    public List<Income> getRecentIncomes(Long userId, int limit) {
        return incomeRepository.findByUserIdOrderByDateDesc(userId)
                .stream()
                .limit(limit)
                .toList();
    }

    private void handleRecurring(Income income, IncomeRequest request) {

        if (Boolean.TRUE.equals(request.getIsRecurring())
                && request.getRecurrenceType() != null
                && request.getDate() != null) {

            income.setNextOccurrence(
                    calculateNextOccurrence(
                            request.getDate(),
                            request.getRecurrenceType()
                    )
            );
        } else {
            income.setNextOccurrence(null);
        }
    }

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
