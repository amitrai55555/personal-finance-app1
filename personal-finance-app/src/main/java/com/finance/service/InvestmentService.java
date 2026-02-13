package com.finance.service;

import com.finance.dto.InvestmentRecommendation;
import com.finance.dto.PortfolioAllocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class InvestmentService {
    
    @Autowired
    private DashboardService dashboardService;
    
    public PortfolioAllocation generatePortfolioRecommendation(Long userId, String riskProfile) {
        // Get user's financial insights
        Map<String, Object> insights = dashboardService.getFinancialInsights(userId);
        BigDecimal investmentCapacity = (BigDecimal) insights.get("investmentCapacity");
        
        // Ensure minimum investment capacity
        if (investmentCapacity.compareTo(BigDecimal.valueOf(100)) < 0) {
            investmentCapacity = BigDecimal.valueOf(100); // Minimum $100 for recommendations
        }
        
        // Create portfolio allocation
        PortfolioAllocation allocation = new PortfolioAllocation(investmentCapacity, riskProfile);
        
        // Generate specific investment recommendations
        List<InvestmentRecommendation> recommendations = generateInvestmentRecommendations(allocation);
        allocation.setRecommendations(recommendations);
        
        return allocation;
    }
    
    private List<InvestmentRecommendation> generateInvestmentRecommendations(PortfolioAllocation allocation) {
        List<InvestmentRecommendation> recommendations = new ArrayList<>();
        
        // Stock recommendations (60% of capacity)
        recommendations.addAll(generateStockRecommendations(allocation.getStocksAllocation(), allocation.getRiskProfile()));
        
        // Bond recommendations (20% of capacity)
        recommendations.addAll(generateBondRecommendations(allocation.getBondsAllocation(), allocation.getRiskProfile()));
        
        // Real Estate recommendations (15% of capacity)
        recommendations.addAll(generateRealEstateRecommendations(allocation.getRealEstateAllocation(), allocation.getRiskProfile()));
        
        // Alternative investments (5% of capacity)
        recommendations.addAll(generateAlternativeRecommendations(allocation.getAlternativesAllocation(), allocation.getRiskProfile()));
        
        return recommendations;
    }
    
    private List<InvestmentRecommendation> generateStockRecommendations(BigDecimal allocation, String riskProfile) {
        List<InvestmentRecommendation> stocks = new ArrayList<>();
        
        if ("CONSERVATIVE".equals(riskProfile)) {
            // Dividend stocks and blue chips
            stocks.add(createRecommendation("VTI", "Vanguard Total Stock Market ETF", "ETF", 
                allocation.multiply(BigDecimal.valueOf(0.4)), 0.85, 
                "Broad market exposure with low fees and consistent performance", "LOW", 7.5,
                Arrays.asList("Diversified exposure", "Low expense ratio", "Reliable long-term returns"),
                Arrays.asList("Lower growth potential", "Market volatility")));
            
            stocks.add(createRecommendation("SCHD", "Schwab US Dividend Equity ETF", "ETF", 
                allocation.multiply(BigDecimal.valueOf(0.3)), 0.82, 
                "Focus on dividend-paying companies with strong fundamentals", "LOW", 8.2,
                Arrays.asList("Regular dividend income", "Quality companies", "Lower volatility"),
                Arrays.asList("Limited growth stocks", "Sector concentration")));
            
            stocks.add(createRecommendation("JNJ", "Johnson & Johnson", "STOCK", 
                allocation.multiply(BigDecimal.valueOf(0.3)), 0.78, 
                "Defensive healthcare stock with consistent dividend payments", "LOW", 6.8,
                Arrays.asList("Stable earnings", "Strong dividend history", "Healthcare resilience"),
                Arrays.asList("Slow growth", "Regulatory risks")));
                
        } else if ("AGGRESSIVE".equals(riskProfile)) {
            // Growth stocks and tech
            stocks.add(createRecommendation("QQQ", "Invesco QQQ Trust", "ETF", 
                allocation.multiply(BigDecimal.valueOf(0.4)), 0.88, 
                "Technology-focused ETF with high growth potential", "HIGH", 12.5,
                Arrays.asList("Tech sector exposure", "High growth potential", "Innovation leaders"),
                Arrays.asList("High volatility", "Sector concentration", "Valuation risk")));
            
            stocks.add(createRecommendation("ARKK", "ARK Innovation ETF", "ETF", 
                allocation.multiply(BigDecimal.valueOf(0.3)), 0.75, 
                "Disruptive innovation companies with breakthrough technologies", "HIGH", 15.0,
                Arrays.asList("Innovation focus", "Disruptive technologies", "High upside potential"),
                Arrays.asList("Very high volatility", "Concentrated holdings", "Speculative")));
            
            stocks.add(createRecommendation("NVDA", "NVIDIA Corporation", "STOCK", 
                allocation.multiply(BigDecimal.valueOf(0.3)), 0.82, 
                "AI and semiconductor leader with strong growth prospects", "HIGH", 18.5,
                Arrays.asList("AI market leader", "Strong financials", "Growing demand"),
                Arrays.asList("High valuation", "Cyclical industry", "Competition risk")));
                
        } else { // MODERATE
            stocks.add(createRecommendation("SPY", "SPDR S&P 500 ETF", "ETF", 
                allocation.multiply(BigDecimal.valueOf(0.5)), 0.90, 
                "Broad S&P 500 exposure with balanced risk-return profile", "MEDIUM", 9.5,
                Arrays.asList("Market benchmark", "Broad diversification", "Low fees"),
                Arrays.asList("Market risk", "No downside protection")));
            
            stocks.add(createRecommendation("VGT", "Vanguard Information Technology ETF", "ETF", 
                allocation.multiply(BigDecimal.valueOf(0.5)), 0.85, 
                "Technology sector exposure with growth potential", "MEDIUM", 11.2,
                Arrays.asList("Tech sector growth", "Quality companies", "Innovation exposure"),
                Arrays.asList("Sector concentration", "Volatility", "Valuation risk")));
        }
        
        return stocks;
    }
    
    private List<InvestmentRecommendation> generateBondRecommendations(BigDecimal allocation, String riskProfile) {
        List<InvestmentRecommendation> bonds = new ArrayList<>();
        
        bonds.add(createRecommendation("BND", "Vanguard Total Bond Market ETF", "ETF", 
            allocation.multiply(BigDecimal.valueOf(0.6)), 0.88, 
            "Broad bond market exposure for stability and income", "LOW", 4.2,
            Arrays.asList("Portfolio stability", "Regular income", "Inflation hedge"),
            Arrays.asList("Interest rate risk", "Lower returns", "Inflation risk")));
        
        bonds.add(createRecommendation("TIPS", "Treasury Inflation-Protected Securities", "BOND", 
            allocation.multiply(BigDecimal.valueOf(0.4)), 0.85, 
            "Inflation-protected government bonds for purchasing power preservation", "LOW", 3.8,
            Arrays.asList("Inflation protection", "Government backing", "Real return preservation"),
            Arrays.asList("Lower nominal yields", "Deflation risk", "Tax implications")));
        
        return bonds;
    }
    
    private List<InvestmentRecommendation> generateRealEstateRecommendations(BigDecimal allocation, String riskProfile) {
        List<InvestmentRecommendation> realEstate = new ArrayList<>();
        
        realEstate.add(createRecommendation("VNQ", "Vanguard Real Estate ETF", "ETF", 
            allocation.multiply(BigDecimal.valueOf(0.7)), 0.82, 
            "Real Estate Investment Trust exposure for diversification", "MEDIUM", 8.5,
            Arrays.asList("Real estate exposure", "Dividend income", "Inflation hedge"),
            Arrays.asList("Interest rate sensitivity", "Property market risk", "Economic cycles")));
        
        realEstate.add(createRecommendation("SCHH", "Schwab US REIT ETF", "ETF", 
            allocation.multiply(BigDecimal.valueOf(0.3)), 0.80, 
            "US REIT exposure with low expense ratio", "MEDIUM", 7.8,
            Arrays.asList("Low fees", "US real estate focus", "Dividend yield"),
            Arrays.asList("Sector concentration", "Interest rate risk", "Property cycles")));
        
        return realEstate;
    }
    
    private List<InvestmentRecommendation> generateAlternativeRecommendations(BigDecimal allocation, String riskProfile) {
        List<InvestmentRecommendation> alternatives = new ArrayList<>();
        
        if ("AGGRESSIVE".equals(riskProfile)) {
            alternatives.add(createRecommendation("BTC", "Bitcoin", "CRYPTO", 
                allocation.multiply(BigDecimal.valueOf(0.6)), 0.65, 
                "Digital gold with high growth potential but extreme volatility", "HIGH", 25.0,
                Arrays.asList("Digital scarcity", "Institutional adoption", "Portfolio diversification"),
                Arrays.asList("Extreme volatility", "Regulatory risk", "Technology risk")));
            
            alternatives.add(createRecommendation("GLD", "SPDR Gold Shares", "COMMODITY", 
                allocation.multiply(BigDecimal.valueOf(0.4)), 0.75, 
                "Gold exposure for inflation protection and portfolio diversification", "MEDIUM", 5.5,
                Arrays.asList("Inflation hedge", "Crisis protection", "Portfolio diversification"),
                Arrays.asList("No income generation", "Storage costs", "Price volatility")));
        } else {
            alternatives.add(createRecommendation("GLD", "SPDR Gold Shares", "COMMODITY", 
                allocation, 0.78, 
                "Gold exposure for stability and inflation protection", "LOW", 4.5,
                Arrays.asList("Inflation hedge", "Portfolio stability", "Crisis protection"),
                Arrays.asList("No income", "Storage costs", "Opportunity cost")));
        }
        
        return alternatives;
    }
    
    private InvestmentRecommendation createRecommendation(String symbol, String name, String type, 
                                                         BigDecimal amount, Double confidence, String reasoning, 
                                                         String riskLevel, Double expectedReturn, 
                                                         List<String> pros, List<String> cons) {
        InvestmentRecommendation recommendation = new InvestmentRecommendation();
        recommendation.setSymbol(symbol);
        recommendation.setName(name);
        recommendation.setType(type);
        recommendation.setRecommendedAmount(amount);
        recommendation.setConfidenceScore(confidence);
        recommendation.setReasoning(reasoning);
        recommendation.setRiskLevel(riskLevel);
        recommendation.setExpectedReturn(expectedReturn);
        recommendation.setPros(pros);
        recommendation.setCons(cons);
        return recommendation;
    }
    
    public BigDecimal calculateInvestmentCapacity(Long userId) {
        Map<String, Object> insights = dashboardService.getFinancialInsights(userId);
        BigDecimal monthlySavings = (BigDecimal) insights.get("monthlySavings");
        
        // 20% of monthly savings for investments
        return monthlySavings.multiply(BigDecimal.valueOf(0.20));
    }
    
    public String determineRiskProfile(Long userId) {
        Map<String, Object> insights = dashboardService.getFinancialInsights(userId);
        BigDecimal monthlySavings = (BigDecimal) insights.get("monthlySavings");
        Double savingsRate = (Double) insights.get("savingsRate");
        
        // Simple risk profiling based on savings rate and capacity
        if (savingsRate > 30.0 && monthlySavings.compareTo(BigDecimal.valueOf(2000)) > 0) {
            return "AGGRESSIVE";
        } else if (savingsRate > 15.0 && monthlySavings.compareTo(BigDecimal.valueOf(500)) > 0) {
            return "MODERATE";
        } else {
            return "CONSERVATIVE";
        }
    }
}
