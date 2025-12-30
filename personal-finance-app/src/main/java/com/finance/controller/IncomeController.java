package com.finance.controller;

import com.finance.dto.IncomeRequest;
import com.finance.entity.Income;
import com.finance.security.UserPrincipal;
import com.finance.service.IncomeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/income")
@CrossOrigin(origins = "*")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    // =====================================================
    // ➕ CREATE INCOME (MANUAL / FRONTEND)
    // =====================================================
    @PostMapping
    public ResponseEntity<?> createIncome(
            @Valid @RequestBody IncomeRequest request,
            Authentication authentication
    ) {

        UserPrincipal userPrincipal =
                (UserPrincipal) authentication.getPrincipal();

        Income income = incomeService.createIncome(
                request,
                userPrincipal.getId()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Income created successfully");
        response.put("income", income);

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 📄 GET ALL INCOMES (OPTIONAL – FOR UI LIST)
    // =====================================================
    @GetMapping
    public ResponseEntity<?> getAllIncomes(Authentication authentication) {

        UserPrincipal userPrincipal =
                (UserPrincipal) authentication.getPrincipal();

        return ResponseEntity.ok(
                incomeService.getAllIncomesByUserId(userPrincipal.getId())
        );
    }
}
