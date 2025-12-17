package com.finance.service;

import com.finance.entity.Expense;
import com.finance.entity.Expense.ExpenseCategory;
import com.finance.entity.Goal;
import com.finance.entity.Income;
import com.finance.entity.Income.IncomeCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    
    @Autowired
    private IncomeService incomeService;
    
    @Autowired
    private ExpenseService expenseService;
    
    @Autowired
    private GoalService goalService;
    
    public Map<String, Object> getDashboardOverview(Long userId) {
        Map<String, Object> overview = new HashMap<>();
        
        // Get current month date range
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        
        // Financial summary
        BigDecimal totalIncome = incomeService.getTotalIncome(userId);
        BigDecimal totalExpenses = expenseService.getTotalExpenses(userId);
        BigDecimal netIncome = totalIncome.subtract(totalExpenses);
        
        // Monthly totals
        BigDecimal monthlyIncome = incomeService.getTotalIncomeByDateRange(userId, monthStart, monthEnd);
        BigDecimal monthlyExpenses = expenseService.getTotalExpensesByDateRange(userId, monthStart, monthEnd);
        BigDecimal monthlyNet = monthlyIncome.subtract(monthlyExpenses);
        
        overview.put("totalIncome", totalIncome);
        overview.put("totalExpenses", totalExpenses);
        overview.put("netIncome", netIncome);
        overview.put("monthlyIncome", monthlyIncome);
        overview.put("monthlyExpenses", monthlyExpenses);
        overview.put("monthlyNet", monthlyNet);
        
        // Goals progress
        List<Goal> activeGoals = goalService.getActiveGoalsByUserId(userId);
        Double averageProgress = goalService.getAverageGoalProgress(userId);
        
        overview.put("activeGoalsCount", activeGoals.size());
        overview.put("averageGoalProgress", averageProgress);
        overview.put("completedGoalsCount", goalService.getGoalCountByStatus(userId, Goal.GoalStatus.COMPLETED));
        
        // Recent transactions
        List<Income> recentIncomes = incomeService.getRecentIncomes(userId, 5);
        List<Expense> recentExpenses = expenseService.getRecentExpenses(userId, 5);
        
        overview.put("recentIncomes", recentIncomes);
        overview.put("recentExpenses", recentExpenses);
        
        return overview;
    }
    
    public Map<String, Object> getSpendingAnalysis(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analysis = new HashMap<>();
        
        // If no dates provided, use current month
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = now.withDayOfMonth(now.lengthOfMonth());
        }
        
        // Expenses by category
        Map<ExpenseCategory, BigDecimal> expensesByCategory = 
            expenseService.getExpensesByCategoryAndDateRange(userId, startDate, endDate);
        
        // Income by category
        Map<IncomeCategory, BigDecimal> incomeByCategory = 
            incomeService.getIncomeByCategoryAndDateRange(userId, startDate, endDate);
        
        // Total amounts
        BigDecimal totalExpenses = expenseService.getTotalExpensesByDateRange(userId, startDate, endDate);
        BigDecimal totalIncome = incomeService.getTotalIncomeByDateRange(userId, startDate, endDate);
        
        analysis.put("expensesByCategory", expensesByCategory);
        analysis.put("incomeByCategory", incomeByCategory);
        analysis.put("totalExpenses", totalExpenses);
        analysis.put("totalIncome", totalIncome);
        analysis.put("netAmount", totalIncome.subtract(totalExpenses));
        analysis.put("startDate", startDate);
        analysis.put("endDate", endDate);
        
        return analysis;
    }
    
    public Map<String, Object> getMonthlyTrends(Long userId, int monthsBack) {
        Map<String, Object> trends = new HashMap<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(monthsBack);
        
        Map<String, BigDecimal> monthlyIncomeData = new HashMap<>();
        Map<String, BigDecimal> monthlyExpenseData = new HashMap<>();
        
        // Generate monthly data
        for (int i = 0; i < monthsBack; i++) {
            YearMonth yearMonth = YearMonth.from(endDate.minusMonths(i));
            LocalDate monthStart = yearMonth.atDay(1);
            LocalDate monthEnd = yearMonth.atEndOfMonth();
            
            String monthKey = yearMonth.toString();
            BigDecimal monthIncome = incomeService.getTotalIncomeByDateRange(userId, monthStart, monthEnd);
            BigDecimal monthExpenses = expenseService.getTotalExpensesByDateRange(userId, monthStart, monthEnd);
            
            monthlyIncomeData.put(monthKey, monthIncome);
            monthlyExpenseData.put(monthKey, monthExpenses);
        }
        
        trends.put("monthlyIncome", monthlyIncomeData);
        trends.put("monthlyExpenses", monthlyExpenseData);
        trends.put("startDate", startDate);
        trends.put("endDate", endDate);
        
        return trends;
    }
    
    public Map<String, Object> getFinancialInsights(Long userId) {
        Map<String, Object> insights = new HashMap<>();
        
        // Current month data
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        
        BigDecimal monthlyIncome = incomeService.getTotalIncomeByDateRange(userId, monthStart, monthEnd);
        BigDecimal monthlyExpenses = expenseService.getTotalExpensesByDateRange(userId, monthStart, monthEnd);
        BigDecimal monthlySavings = monthlyIncome.subtract(monthlyExpenses);
        
        // Previous month for comparison
        LocalDate prevMonthStart = monthStart.minusMonths(1);
        LocalDate prevMonthEnd = prevMonthStart.withDayOfMonth(prevMonthStart.lengthOfMonth());
        
        BigDecimal prevMonthIncome = incomeService.getTotalIncomeByDateRange(userId, prevMonthStart, prevMonthEnd);
        BigDecimal prevMonthExpenses = expenseService.getTotalExpensesByDateRange(userId, prevMonthStart, prevMonthEnd);
        
        // Calculate savings rate
        double savingsRate = monthlyIncome.compareTo(BigDecimal.ZERO) > 0 ? 
            monthlySavings.multiply(BigDecimal.valueOf(100)).divide(monthlyIncome, 2, BigDecimal.ROUND_HALF_UP).doubleValue() : 0.0;
        
        // Top expense categories
        Map<ExpenseCategory, BigDecimal> expensesByCategory = expenseService.getExpensesByCategory(userId);
        List<Map<String, Object>> topExpenseCategories = expensesByCategory.entrySet().stream()
            .sorted(Map.Entry.<ExpenseCategory, BigDecimal>comparingByValue().reversed())
            .limit(5)
            .map(entry -> {
                Map<String, Object> categoryData = new HashMap<>();
                categoryData.put("category", entry.getKey());
                categoryData.put("amount", entry.getValue());
                return categoryData;
            })
            .collect(Collectors.toList());
        
        insights.put("monthlyIncome", monthlyIncome);
        insights.put("monthlyExpenses", monthlyExpenses);
        insights.put("monthlySavings", monthlySavings);
        insights.put("savingsRate", savingsRate);
        insights.put("prevMonthIncome", prevMonthIncome);
        insights.put("prevMonthExpenses", prevMonthExpenses);
        insights.put("topExpenseCategories", topExpenseCategories);
        
        // Investment capacity (20% of net income)
        BigDecimal investmentCapacity = monthlySavings.multiply(BigDecimal.valueOf(0.20));
        insights.put("investmentCapacity", investmentCapacity);
        
        return insights;
    }
}
