package com.finance.service;

import com.finance.dto.IncomeRequest;
import com.finance.entity.BankAccount;
import com.finance.entity.Income;
import com.finance.entity.User;
import com.finance.repository.BankAccountRepository;
import com.finance.repository.IncomeRepository;
import com.finance.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;

    public IncomeService(
            IncomeRepository incomeRepository,
            UserRepository userRepository,
            BankAccountRepository bankAccountRepository
    ) {
        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    // =====================================================
    // 1️⃣ CREATE INCOME (FRONTEND / MANUAL ENTRY)
    // =====================================================
    public Income createIncomeFromBank(IncomeRequest request, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount bankAccount = bankAccountRepository
                .findPrimaryVerifiedAccountByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No verified bank account found"));

        Income income = new Income();
        income.setDescription(request.getDescription());
        income.setAmount(request.getAmount());
        income.setCategory(request.getCategory());
        income.setDate(request.getDate());
        income.setIsRecurring(request.getIsRecurring());
        income.setRecurrenceType(request.getRecurrenceType());
        income.setNotes(request.getNotes());
        income.setUser(user);
        income.setBankAccount(bankAccount);

        if (Boolean.TRUE.equals(request.getIsRecurring()) && request.getRecurrenceType() != null) {
            income.setNextOccurrence(
                    calculateNextOccurrence(request.getDate(), request.getRecurrenceType())
            );
        }

        return incomeRepository.save(income);
    }

    // =====================================================
    // 2️⃣ CREATE INCOME (ACCOUNT AGGREGATOR / BANK SYNC)
    // =====================================================
    public Income createIncome(
            IncomeRequest request,
            Long userId,
            BankAccount bankAccount
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Income income = new Income();
        income.setDescription(request.getDescription());
        income.setAmount(request.getAmount());
        income.setCategory(request.getCategory());
        income.setDate(request.getDate());
        income.setIsRecurring(false);
        income.setUser(user);
        income.setBankAccount(bankAccount);

        return incomeRepository.save(income);
    }
    public List<Income> getAllIncomesByUserId(Long userId) {
        return incomeRepository.findByUserIdOrderByDateDesc(userId);
    }


    // =====================================================
    // 🔁 HELPER METHOD
    // =====================================================
    private LocalDate calculateNextOccurrence(
            LocalDate date,
            Income.RecurrenceType recurrenceType
    ) {
        return switch (recurrenceType) {
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
            case QUARTERLY -> date.plusMonths(3);
            case YEARLY -> date.plusYears(1);
        };
    }


}
