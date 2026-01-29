package com.finance.service;

import com.finance.dto.ExpenseResponse;
import com.finance.dto.IncomeResponse;
import com.finance.entity.Expense;
import com.finance.entity.Income;
import com.finance.entity.Goal;
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

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final GoalService goalService;

    public DashboardService(
            IncomeService incomeService,
            ExpenseService expenseService,
            GoalService goalService
    ) {
        this.incomeService = incomeService;
        this.expenseService = expenseService;
        this.goalService = goalService;
    }


    public Map<String, Object> getDashboardOverview(Long userId) {

        Map<String, Object> response = new HashMap<>();

        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());

        // Totals
        BigDecimal totalIncome = incomeService.getTotalIncome(userId);
        BigDecimal totalExpenses = expenseService.getTotalExpenses(userId);

        BigDecimal monthlyIncome =
                incomeService.getTotalIncomeByDateRange(userId, monthStart, monthEnd);

        BigDecimal monthlyExpenses =
                expenseService.getTotalExpensesByDateRange(userId, monthStart, monthEnd);

        response.put("totalIncome", totalIncome);
        response.put("totalExpenses", totalExpenses);
        response.put("netBalance", totalIncome.subtract(totalExpenses));
        response.put("monthlyIncome", monthlyIncome);
        response.put("monthlyExpenses", monthlyExpenses);
        response.put("monthlyNet", monthlyIncome.subtract(monthlyExpenses));

        // Recent Incomes
        List<IncomeResponse> recentIncomes =
                incomeService.getRecentIncomes(userId, 5)
                        .stream()
                        .map(this::mapIncomeToDto)
                        .toList();

        // Recent Expenses
        List<ExpenseResponse> recentExpenses =
                expenseService.getRecentExpenses(userId, 5)
                        .stream()
                        .map(this::mapExpenseToDto)
                        .toList();

        response.put("recentIncomes", recentIncomes);
        response.put("recentExpenses", recentExpenses);

        // Goals
        List<Goal> activeGoals = goalService.getActiveGoalsByUserId(userId);

        response.put("activeGoalsCount", activeGoals.size());
        response.put("completedGoalsCount",
                goalService.getGoalCountByStatus(
                        userId, Goal.GoalStatus.COMPLETED
                )
        );

        return response;
    }


    public Map<String, Object> getMonthlyTrends(Long userId, int monthsBack) {

        Map<String, Object> result = new HashMap<>();
        Map<String, BigDecimal> incomeMap = new HashMap<>();
        Map<String, BigDecimal> expenseMap = new HashMap<>();

        for (int i = 0; i < monthsBack; i++) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            incomeMap.put(
                    ym.toString(),
                    incomeService.getTotalIncomeByDateRange(userId, start, end)
            );

            expenseMap.put(
                    ym.toString(),
                    expenseService.getTotalExpensesByDateRange(userId, start, end)
            );
        }

        result.put("income", incomeMap);
        result.put("expenses", expenseMap);

        return result;
    }

    public Map<String, Object> getSpendingAnalysis(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Map<String, Object> data = new HashMap<>();

        Map<?, BigDecimal> expensesByCategory =
                expenseService.getExpensesByCategoryAndDateRange(
                        userId, startDate, endDate
                );

        Map<?, BigDecimal> incomeByCategory =
                incomeService.getIncomeByCategoryAndDateRange(
                        userId, startDate, endDate
                );

        data.put("expensesByCategory", expensesByCategory);
        data.put("incomeByCategory", incomeByCategory);
        data.put("totalExpenses",
                expenseService.getTotalExpensesByDateRange(userId, startDate, endDate));
        data.put("totalIncome",
                incomeService.getTotalIncomeByDateRange(userId, startDate, endDate));

        return data;
    }


    public Map<String, Object> getFinancialInsights(Long userId) {
        Map<String, Object> insights = new HashMap<>();

        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());

        BigDecimal monthlyIncome = incomeService.getTotalIncomeByDateRange(userId, monthStart, monthEnd);
        BigDecimal monthlyExpenses = expenseService.getTotalExpensesByDateRange(userId, monthStart, monthEnd);
        BigDecimal monthlySavings = monthlyIncome.subtract(monthlyExpenses);

        double savingsRate = BigDecimal.ZERO.compareTo(monthlyIncome) == 0
                ? 0.0
                : monthlySavings
                .divide(monthlyIncome, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        // Basic rules used by InvestmentService
        BigDecimal investmentCapacity = monthlySavings.multiply(BigDecimal.valueOf(0.20));

        insights.put("monthlyIncome", monthlyIncome);
        insights.put("monthlyExpenses", monthlyExpenses);
        insights.put("monthlySavings", monthlySavings);
        insights.put("savingsRate", savingsRate);
        insights.put("investmentCapacity", investmentCapacity);

        return insights;
    }


    private IncomeResponse mapIncomeToDto(Income i) {
        IncomeResponse dto = new IncomeResponse();
        dto.setId(i.getId());
        dto.setDescription(i.getDescription());
        dto.setAmount(i.getAmount());
        dto.setDate(i.getDate());
        dto.setCategory(i.getCategory().name());
        return dto;
    }

    private ExpenseResponse mapExpenseToDto(Expense e) {
        ExpenseResponse dto = new ExpenseResponse();
        dto.setId(e.getId());
        dto.setDescription(e.getDescription());
        dto.setAmount(e.getAmount());
        dto.setDate(e.getDate());
        dto.setCategory(e.getCategory().name());
        return dto;
    }
}
