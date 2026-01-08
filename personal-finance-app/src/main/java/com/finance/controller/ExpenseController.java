package com.finance.controller;

import com.finance.dto.ExpenseRequest;
import com.finance.entity.Expense;
import com.finance.security.UserPrincipal;
import com.finance.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/expense")
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // =====================================================
    // ➕ CREATE EXPENSE
    // =====================================================
    @PostMapping
    public ResponseEntity<?> createExpense(
            @Valid @RequestBody ExpenseRequest request,
            Authentication authentication
    ) {

        UserPrincipal userPrincipal =
                (UserPrincipal) authentication.getPrincipal();

        Expense expense = expenseService.createExpense(
                request,
                userPrincipal.getId()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Expense created successfully");
        response.put("expense", expense);

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 📄 GET ALL EXPENSES
    // =====================================================
    @GetMapping
    public ResponseEntity<?> getAllExpenses(
            Authentication authentication
    ) {
        UserPrincipal userPrincipal =
                (UserPrincipal) authentication.getPrincipal();

        return ResponseEntity.ok(
                expenseService.getAllExpensesByUserId(
                        userPrincipal.getId()
                )
        );
    }

    // =====================================================
    // ✏ UPDATE EXPENSE
    // =====================================================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Expense updated = expenseService.updateExpense(
                id,
                request,
                userPrincipal.getId()
        );

        return ResponseEntity.ok(updated);
    }

    // =====================================================
    // ❌ DELETE EXPENSE
    // =====================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        expenseService.deleteExpense(id, userPrincipal.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Expense deleted successfully");
        return ResponseEntity.ok(response);
    }
}
