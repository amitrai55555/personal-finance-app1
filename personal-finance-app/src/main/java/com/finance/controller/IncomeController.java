package com.finance.controller;

import com.finance.dto.IncomeRequest;
import com.finance.entity.Income;
import com.finance.entity.Income.IncomeCategory;
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
    
    @PostMapping
    public ResponseEntity<?> createIncome(@Valid @RequestBody IncomeRequest request, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Income income = incomeService.createIncome(request, userPrincipal.getId());
            return ResponseEntity.ok(income);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Income>> getAllIncomes(Authentication authentication,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        if (page == 0 && size == 10) {
            // Return all incomes if no pagination specified
            List<Income> incomes = incomeService.getAllIncomesByUserId(userPrincipal.getId());
            return ResponseEntity.ok(incomes);
        } else {
            // Return paginated results
            Pageable pageable = PageRequest.of(page, size);
            Page<Income> incomePage = incomeService.getIncomesByUserId(userPrincipal.getId(), pageable);
            return ResponseEntity.ok(incomePage.getContent());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getIncomeById(@PathVariable Long id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return incomeService.getIncomeById(id, userPrincipal.getId())
                .map(income -> ResponseEntity.ok(income))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateIncome(@PathVariable Long id, 
                                         @Valid @RequestBody IncomeRequest request, 
                                         Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Income updatedIncome = incomeService.updateIncome(id, request, userPrincipal.getId());
            return ResponseEntity.ok(updatedIncome);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIncome(@PathVariable Long id, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            incomeService.deleteIncome(id, userPrincipal.getId());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Income deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/total")
    public ResponseEntity<Map<String, BigDecimal>> getTotalIncome(Authentication authentication,
                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        BigDecimal total;
        
        if (startDate != null && endDate != null) {
            total = incomeService.getTotalIncomeByDateRange(userPrincipal.getId(), startDate, endDate);
        } else {
            total = incomeService.getTotalIncome(userPrincipal.getId());
        }
        
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("total", total);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/by-category")
    public ResponseEntity<Map<IncomeCategory, BigDecimal>> getIncomeByCategory(Authentication authentication,
                                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Map<IncomeCategory, BigDecimal> categoryBreakdown;
        
        if (startDate != null && endDate != null) {
            categoryBreakdown = incomeService.getIncomeByCategoryAndDateRange(userPrincipal.getId(), startDate, endDate);
        } else {
            categoryBreakdown = incomeService.getIncomeByCategory(userPrincipal.getId());
        }
        
        return ResponseEntity.ok(categoryBreakdown);
    }
    
    @GetMapping("/recent")
    public ResponseEntity<List<Income>> getRecentIncomes(Authentication authentication,
                                                        @RequestParam(defaultValue = "10") int limit) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<Income> recentIncomes = incomeService.getRecentIncomes(userPrincipal.getId(), limit);
        return ResponseEntity.ok(recentIncomes);
    }
    
    @GetMapping("/categories")
    public ResponseEntity<IncomeCategory[]> getIncomeCategories() {
        return ResponseEntity.ok(IncomeCategory.values());
    }
}
