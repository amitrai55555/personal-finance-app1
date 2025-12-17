package com.finance.controller;

import com.finance.dto.PortfolioAllocation;
import com.finance.security.UserPrincipal;
import com.finance.service.InvestmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/investments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InvestmentController {
    
    @Autowired
    private InvestmentService investmentService;
    
    @GetMapping("/recommendations")
    public ResponseEntity<PortfolioAllocation> getInvestmentRecommendations(
            Authentication authentication,
            @RequestParam(defaultValue = "MODERATE") String riskProfile) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        PortfolioAllocation recommendations = investmentService.generatePortfolioRecommendation(
            userPrincipal.getId(), riskProfile.toUpperCase());
        
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/capacity")
    public ResponseEntity<Map<String, Object>> getInvestmentCapacity(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        BigDecimal capacity = investmentService.calculateInvestmentCapacity(userPrincipal.getId());
        String suggestedRiskProfile = investmentService.determineRiskProfile(userPrincipal.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("investmentCapacity", capacity);
        response.put("suggestedRiskProfile", suggestedRiskProfile);
        response.put("description", "Based on 20% of your monthly net income");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/risk-profiles")
    public ResponseEntity<Map<String, Object>> getRiskProfiles() {
        Map<String, Object> profiles = new HashMap<>();
        
        Map<String, Object> conservative = new HashMap<>();
        conservative.put("name", "Conservative");
        conservative.put("description", "Lower risk, steady returns");
        conservative.put("stocksPercentage", 40);
        conservative.put("bondsPercentage", 40);
        conservative.put("realEstatePercentage", 15);
        conservative.put("alternativesPercentage", 5);
        conservative.put("expectedReturn", "5-7%");
        conservative.put("riskLevel", "Low");
        
        Map<String, Object> moderate = new HashMap<>();
        moderate.put("name", "Moderate");
        moderate.put("description", "Balanced risk and return");
        moderate.put("stocksPercentage", 60);
        moderate.put("bondsPercentage", 20);
        moderate.put("realEstatePercentage", 15);
        moderate.put("alternativesPercentage", 5);
        moderate.put("expectedReturn", "7-10%");
        moderate.put("riskLevel", "Medium");
        
        Map<String, Object> aggressive = new HashMap<>();
        aggressive.put("name", "Aggressive");
        aggressive.put("description", "Higher risk, higher potential returns");
        aggressive.put("stocksPercentage", 80);
        aggressive.put("bondsPercentage", 10);
        aggressive.put("realEstatePercentage", 5);
        aggressive.put("alternativesPercentage", 5);
        aggressive.put("expectedReturn", "10-15%");
        aggressive.put("riskLevel", "High");
        
        profiles.put("CONSERVATIVE", conservative);
        profiles.put("MODERATE", moderate);
        profiles.put("AGGRESSIVE", aggressive);
        
        return ResponseEntity.ok(profiles);
    }
}
