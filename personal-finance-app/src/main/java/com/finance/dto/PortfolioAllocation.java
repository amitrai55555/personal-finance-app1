package com.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public class PortfolioAllocation {
    
    private BigDecimal totalInvestmentCapacity;
    private BigDecimal stocksAllocation;
    private BigDecimal bondsAllocation;
    private BigDecimal realEstateAllocation;
    private BigDecimal alternativesAllocation;
    private Double stocksPercentage;
    private Double bondsPercentage;
    private Double realEstatePercentage;
    private Double alternativesPercentage;
    private String riskProfile;
    private List<InvestmentRecommendation> recommendations;
    
    // Constructors
    public PortfolioAllocation() {}
    
    public PortfolioAllocation(BigDecimal totalInvestmentCapacity, String riskProfile) {
        this.totalInvestmentCapacity = totalInvestmentCapacity;
        this.riskProfile = riskProfile;
        calculateAllocations();
    }
    
    private void calculateAllocations() {
        // Default allocation: 60% stocks, 20% bonds, 15% real estate, 5% alternatives
        this.stocksPercentage = 60.0;
        this.bondsPercentage = 20.0;
        this.realEstatePercentage = 15.0;
        this.alternativesPercentage = 5.0;
        
        // Adjust based on risk profile
        switch (riskProfile) {
            case "CONSERVATIVE":
                this.stocksPercentage = 40.0;
                this.bondsPercentage = 40.0;
                this.realEstatePercentage = 15.0;
                this.alternativesPercentage = 5.0;
                break;
            case "AGGRESSIVE":
                this.stocksPercentage = 80.0;
                this.bondsPercentage = 10.0;
                this.realEstatePercentage = 5.0;
                this.alternativesPercentage = 5.0;
                break;
            default: // MODERATE
                break;
        }
        
        this.stocksAllocation = totalInvestmentCapacity.multiply(BigDecimal.valueOf(stocksPercentage / 100));
        this.bondsAllocation = totalInvestmentCapacity.multiply(BigDecimal.valueOf(bondsPercentage / 100));
        this.realEstateAllocation = totalInvestmentCapacity.multiply(BigDecimal.valueOf(realEstatePercentage / 100));
        this.alternativesAllocation = totalInvestmentCapacity.multiply(BigDecimal.valueOf(alternativesPercentage / 100));
    }
    
    // Getters and Setters
    public BigDecimal getTotalInvestmentCapacity() {
        return totalInvestmentCapacity;
    }
    
    public void setTotalInvestmentCapacity(BigDecimal totalInvestmentCapacity) {
        this.totalInvestmentCapacity = totalInvestmentCapacity;
    }
    
    public BigDecimal getStocksAllocation() {
        return stocksAllocation;
    }
    
    public void setStocksAllocation(BigDecimal stocksAllocation) {
        this.stocksAllocation = stocksAllocation;
    }
    
    public BigDecimal getBondsAllocation() {
        return bondsAllocation;
    }
    
    public void setBondsAllocation(BigDecimal bondsAllocation) {
        this.bondsAllocation = bondsAllocation;
    }
    
    public BigDecimal getRealEstateAllocation() {
        return realEstateAllocation;
    }
    
    public void setRealEstateAllocation(BigDecimal realEstateAllocation) {
        this.realEstateAllocation = realEstateAllocation;
    }
    
    public BigDecimal getAlternativesAllocation() {
        return alternativesAllocation;
    }
    
    public void setAlternativesAllocation(BigDecimal alternativesAllocation) {
        this.alternativesAllocation = alternativesAllocation;
    }
    
    public Double getStocksPercentage() {
        return stocksPercentage;
    }
    
    public void setStocksPercentage(Double stocksPercentage) {
        this.stocksPercentage = stocksPercentage;
    }
    
    public Double getBondsPercentage() {
        return bondsPercentage;
    }
    
    public void setBondsPercentage(Double bondsPercentage) {
        this.bondsPercentage = bondsPercentage;
    }
    
    public Double getRealEstatePercentage() {
        return realEstatePercentage;
    }
    
    public void setRealEstatePercentage(Double realEstatePercentage) {
        this.realEstatePercentage = realEstatePercentage;
    }
    
    public Double getAlternativesPercentage() {
        return alternativesPercentage;
    }
    
    public void setAlternativesPercentage(Double alternativesPercentage) {
        this.alternativesPercentage = alternativesPercentage;
    }
    
    public String getRiskProfile() {
        return riskProfile;
    }
    
    public void setRiskProfile(String riskProfile) {
        this.riskProfile = riskProfile;
    }
    
    public List<InvestmentRecommendation> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(List<InvestmentRecommendation> recommendations) {
        this.recommendations = recommendations;
    }
}
