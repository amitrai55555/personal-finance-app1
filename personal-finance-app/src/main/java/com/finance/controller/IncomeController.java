package com.finance.controller;

import com.finance.dto.IncomeRequest;
import com.finance.entity.Income;
import com.finance.security.UserPrincipal;
import com.finance.service.IncomeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/income")
@CrossOrigin(origins = "*", maxAge = 3600)
public class IncomeController {

    @Autowired
    private IncomeService incomeService;

    // ✅ CREATE INCOME
    @PostMapping
    public ResponseEntity<?> createIncome(
            @Valid @RequestBody IncomeRequest request,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Income income = incomeService.createIncome(request, userPrincipal.getId());
        return ResponseEntity.ok(income);
    }

    // ✅ GET ALL / PAGINATED INCOMES
    @GetMapping
    public ResponseEntity<List<Income>> getAllIncomes(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        if (page == 0 && size == 10) {
            return ResponseEntity.ok(
                    incomeService.getAllIncomesByUserId(userPrincipal.getId())
            );
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Income> incomePage =
                incomeService.getIncomesByUserId(userPrincipal.getId(), pageable);

        return ResponseEntity.ok(incomePage.getContent());
    }

    // ✅ GET INCOME BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getIncomeById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return incomeService.getIncomeById(id, userPrincipal.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ UPDATE INCOME
    @PutMapping("/{id}")
    public ResponseEntity<?> updateIncome(
            @PathVariable Long id,
            @Valid @RequestBody IncomeRequest request,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Income updatedIncome =
                incomeService.updateIncome(id, request, userPrincipal.getId());
        System.out.println("income updated successfully");
        return ResponseEntity.ok(updatedIncome);
    }

    // ✅ DELETE INCOME
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIncome(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        incomeService.deleteIncome(id, userPrincipal.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Income deleted successfully");
        return ResponseEntity.ok(response);
    }

    // ✅ TOTAL INCOME
    @GetMapping("/total")
    public ResponseEntity<Map<String, BigDecimal>> getTotalIncome(
            Authentication authentication,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        BigDecimal total = (startDate != null && endDate != null)
                ? incomeService.getTotalIncomeByDateRange(
                userPrincipal.getId(), startDate, endDate)
                : incomeService.getTotalIncome(userPrincipal.getId());

        return ResponseEntity.ok(Map.of("total", total));
    }

    // ✅ INCOME BY CATEGORY
    @GetMapping("/by-category")
    public ResponseEntity<Map<Income.IncomeCategory, BigDecimal>> getIncomeByCategory(
            Authentication authentication,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Map<Income.IncomeCategory, BigDecimal> result =
                (startDate != null && endDate != null)
                        ? incomeService.getIncomeByCategoryAndDateRange(
                        userPrincipal.getId(), startDate, endDate)
                        : incomeService.getIncomeByCategory(userPrincipal.getId());

        return ResponseEntity.ok(result);
    }

    // ✅ RECENT INCOMES
    @GetMapping("/recent")
    public ResponseEntity<List<Income>> getRecentIncomes(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(
                incomeService.getRecentIncomes(userPrincipal.getId(), limit)
        );
    }

    // ✅ INCOME CATEGORIES
    @GetMapping("/categories")
    public ResponseEntity<Income.IncomeCategory[]> getIncomeCategories() {
        return ResponseEntity.ok(Income.IncomeCategory.values());
    }
}
