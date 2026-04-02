package com.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public class InvestmentRecommendation {
    
    private String symbol;
    private String name;
    private String type; // STOCK, ETF, CRYPTO, BOND, REAL_ESTATE
    private BigDecimal recommendedAmount;
    private Double confidenceScore; // 0.0 to 1.0
    private String reasoning;
    private String riskLevel; // LOW, MEDIUM, HIGH
    private Double expectedReturn; // Annual percentage
    private List<String> pros;
    private List<String> cons;
    
    // Constructors
    public InvestmentRecommendation() {}
    
    public InvestmentRecommendation(String symbol, String name, String type, BigDecimal recommendedAmount, 
                                   Double confidenceScore, String reasoning, String riskLevel, Double expectedReturn) {
        this.symbol = symbol;
        this.name = name;
        this.type = type;
        this.recommendedAmount = recommendedAmount;
        this.confidenceScore = confidenceScore;
        this.reasoning = reasoning;
        this.riskLevel = riskLevel;
        this.expectedReturn = expectedReturn;
    }

    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public BigDecimal getRecommendedAmount() {
        return recommendedAmount;
    }
    
    public void setRecommendedAmount(BigDecimal recommendedAmount) {
        this.recommendedAmount = recommendedAmount;
    }
    
    public Double getConfidenceScore() {
        return confidenceScore;
    }
    
    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    
    public String getReasoning() {
        return reasoning;
    }
    
    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public Double getExpectedReturn() {
        return expectedReturn;
    }
    
    public void setExpectedReturn(Double expectedReturn) {
        this.expectedReturn = expectedReturn;
    }
    
    public List<String> getPros() {
        return pros;
    }
    
    public void setPros(List<String> pros) {
        this.pros = pros;
    }
    
    public List<String> getCons() {
        return cons;
    }
    
    public void setCons(List<String> cons) {
        this.cons = cons;
    }
}
