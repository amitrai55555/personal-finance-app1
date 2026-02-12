package com.finance.controller;

import com.finance.dto.coach.CoachAdviceResponse;
import com.finance.security.UserPrincipal;
import com.finance.service.FinancialCoachService;
import com.finance.service.InvestmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coach")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FinancialCoachController {

    private final FinancialCoachService coachService;
    private final InvestmentService investmentService;

    public FinancialCoachController(FinancialCoachService coachService, InvestmentService investmentService) {
        this.coachService = coachService;
        this.investmentService = investmentService;
    }

    @GetMapping("/advice")
    public ResponseEntity<CoachAdviceResponse> advice(
            Authentication authentication,
            @RequestParam(required = false) String riskProfile
    ) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        String resolvedRisk = (riskProfile == null || riskProfile.isBlank())
                ? investmentService.determineRiskProfile(user.getId())
                : riskProfile.toUpperCase();
        return ResponseEntity.ok(coachService.getCoachAdvice(user.getId(), resolvedRisk));
    }
}

