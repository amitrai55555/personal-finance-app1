package com.finance.controller;

import com.finance.dto.ExpenseRequest;
import com.finance.entity.Expense;
import com.finance.entity.Expense.ExpenseCategory;
import com.finance.security.UserPrincipal;
import com.finance.service.BankAccountService;
import com.finance.service.ExpenseService;
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
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ExpenseController {
    
    @Autowired
    private ExpenseService expenseService;
    
    @PostMapping
    public ResponseEntity<?> createExpense(@Valid @RequestBody ExpenseRequest request, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Expense expense = expenseService.createExpense(request, userPrincipal.getId());
            return ResponseEntity.ok(expense);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Expense>> getAllExpenses(Authentication authentication,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        if (page == 0 && size == 10) {
            // Return all expenses if no pagination specified
            List<Expense> expenses = expenseService.getAllExpensesByUserId(userPrincipal.getId());
            return ResponseEntity.ok(expenses);
        } else {
            // Return paginated results
            Pageable pageable = PageRequest.of(page, size);
            Page<Expense> expensePage = expenseService.getExpensesByUserId(userPrincipal.getId(), pageable);
            return ResponseEntity.ok(expensePage.getContent());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getExpenseById(@PathVariable Long id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return expenseService.getExpenseById(id, userPrincipal.getId())
                .map(expense -> ResponseEntity.ok(expense))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpense(@PathVariable Long id, 
                                          @Valid @RequestBody ExpenseRequest request, 
                                          Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Expense updatedExpense = expenseService.updateExpense(id, request, userPrincipal.getId());
            return ResponseEntity.ok(updatedExpense);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            expenseService.deleteExpense(id, userPrincipal.getId());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Expense deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/total")
    public ResponseEntity<Map<String, BigDecimal>> getTotalExpenses(Authentication authentication,
                                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        BigDecimal total;
        
        if (startDate != null && endDate != null) {
            total = expenseService.getTotalExpensesByDateRange(userPrincipal.getId(), startDate, endDate);
        } else {
            total = expenseService.getTotalExpenses(userPrincipal.getId());
        }
        
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("total", total);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/by-category")
    public ResponseEntity<Map<ExpenseCategory, BigDecimal>> getExpensesByCategory(Authentication authentication,
                                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Map<ExpenseCategory, BigDecimal> categoryBreakdown;
        
        if (startDate != null && endDate != null) {
            categoryBreakdown = expenseService.getExpensesByCategoryAndDateRange(userPrincipal.getId(), startDate, endDate);
        } else {
            categoryBreakdown = expenseService.getExpensesByCategory(userPrincipal.getId());
        }
        
        return ResponseEntity.ok(categoryBreakdown);
    }
    
    @GetMapping("/recent")
    public ResponseEntity<List<Expense>> getRecentExpenses(Authentication authentication,
                                                          @RequestParam(defaultValue = "10") int limit) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<Expense> recentExpenses = expenseService.getRecentExpenses(userPrincipal.getId(), limit);
        return ResponseEntity.ok(recentExpenses);
    }
    
    @GetMapping("/categories")
    public ResponseEntity<ExpenseCategory[]> getExpenseCategories() {
        return ResponseEntity.ok(ExpenseCategory.values());
    }
}
