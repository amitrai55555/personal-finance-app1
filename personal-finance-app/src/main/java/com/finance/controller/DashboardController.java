package com.finance.controller;

import com.finance.security.UserPrincipal;
import com.finance.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Map<String, Object> overview = dashboardService.getDashboardOverview(userPrincipal.getId());
        return ResponseEntity.ok(overview);
    }
    
    @GetMapping("/spending-analysis")
    public ResponseEntity<Map<String, Object>> getSpendingAnalysis(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Map<String, Object> analysis = dashboardService.getSpendingAnalysis(userPrincipal.getId(), startDate, endDate);
        return ResponseEntity.ok(analysis);
    }
    
    @GetMapping("/monthly-trends")
    public ResponseEntity<Map<String, Object>> getMonthlyTrends(
            Authentication authentication,
            @RequestParam(defaultValue = "6") int monthsBack) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Map<String, Object> trends = dashboardService.getMonthlyTrends(userPrincipal.getId(), monthsBack);
        return ResponseEntity.ok(trends);
    }
    
    @GetMapping("/insights")
    public ResponseEntity<Map<String, Object>> getFinancialInsights(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Map<String, Object> insights = dashboardService.getFinancialInsights(userPrincipal.getId());
        return ResponseEntity.ok(insights);
    }
}
