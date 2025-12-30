package com.finance.config;

import com.finance.entity.Expense;
import com.finance.entity.Goal;
import com.finance.entity.Income;
import com.finance.entity.User;
import com.finance.repository.ExpenseRepository;
import com.finance.repository.GoalRepository;
import com.finance.repository.IncomeRepository;
import com.finance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ✅ 1. Ensure ADMIN user exists (FIXED)
        if (!userRepository.existsByUsername("admin")) {

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("amit230604@gmail.com"); // ✅ VALID EMAIL
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole(User.Role.ADMIN);

            userRepository.save(admin);
            System.out.println("✅ Admin user created (admin / admin123)");
        }

        // ✅ 2. Prevent duplicate demo data
        if (userRepository.count() > 1) {
            System.out.println("ℹ️ Sample data already exists. Skipping demo data.");
            return;
        }

        // ✅ 3. Create DEMO user
        User user = new User();
        user.setUsername("demo");
        user.setEmail("amitrai8736869660@gmail.com"); // ✅ VALID
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFirstName("Demo");
        user.setLastName("User");
        user.setRole(User.Role.USER); // ✅ Explicit role
        user = userRepository.save(user);

        // ================= INCOME =================
        Income salary = new Income();
        salary.setDescription("Monthly Salary");
        salary.setAmount(BigDecimal.valueOf(5000));
        salary.setCategory(Income.IncomeCategory.SALARY);
        salary.setDate(LocalDate.now().minusDays(15));
        salary.setIsRecurring(true);
        salary.setRecurrenceType(Income.RecurrenceType.MONTHLY);
        salary.setUser(user);
        incomeRepository.save(salary);

        Income freelance = new Income();
        freelance.setDescription("Freelance Project");
        freelance.setAmount(BigDecimal.valueOf(1500));
        freelance.setCategory(Income.IncomeCategory.FREELANCE);
        freelance.setDate(LocalDate.now().minusDays(10));
        freelance.setUser(user);
        incomeRepository.save(freelance);

        // ================= EXPENSE =================
        Expense rent = new Expense();
        rent.setDescription("Monthly Rent");
        rent.setAmount(BigDecimal.valueOf(1200));
        rent.setCategory(Expense.ExpenseCategory.HOUSING);
        rent.setDate(LocalDate.now().minusDays(20));
        rent.setIsRecurring(true);
        rent.setRecurrenceType(Expense.RecurrenceType.MONTHLY);
        rent.setUser(user);
        expenseRepository.save(rent);

        Expense groceries = new Expense();
        groceries.setDescription("Weekly Groceries");
        groceries.setAmount(BigDecimal.valueOf(150));
        groceries.setCategory(Expense.ExpenseCategory.FOOD);
        groceries.setDate(LocalDate.now().minusDays(3));
        groceries.setUser(user);
        expenseRepository.save(groceries);

        Expense gas = new Expense();
        gas.setDescription("Gas Fill-up");
        gas.setAmount(BigDecimal.valueOf(60));
        gas.setCategory(Expense.ExpenseCategory.TRANSPORTATION);
        gas.setDate(LocalDate.now().minusDays(5));
        gas.setUser(user);
        expenseRepository.save(gas);

        // ================= GOALS =================
        Goal emergencyFund = new Goal();
        emergencyFund.setTitle("Emergency Fund");
        emergencyFund.setDescription("Build emergency fund for 6 months of expenses");
        emergencyFund.setTargetAmount(BigDecimal.valueOf(10000));
        emergencyFund.setCurrentAmount(BigDecimal.valueOf(2500));
        emergencyFund.setTargetDate(LocalDate.now().plusMonths(8));
        emergencyFund.setPriority(Goal.Priority.HIGH);
        emergencyFund.setUser(user);
        goalRepository.save(emergencyFund);

        Goal vacation = new Goal();
        vacation.setTitle("Vacation Fund");
        vacation.setDescription("Save for a trip to Europe");
        vacation.setTargetAmount(BigDecimal.valueOf(5000));
        vacation.setCurrentAmount(BigDecimal.valueOf(800));
        vacation.setTargetDate(LocalDate.now().plusMonths(12));
        vacation.setPriority(Goal.Priority.MEDIUM);
        vacation.setUser(user);
        goalRepository.save(vacation);

        Goal newCar = new Goal();
        newCar.setTitle("New Car");
        newCar.setDescription("Save for a down payment on a new car");
        newCar.setTargetAmount(BigDecimal.valueOf(15000));
        newCar.setCurrentAmount(BigDecimal.valueOf(3000));
        newCar.setTargetDate(LocalDate.now().plusMonths(18));
        newCar.setPriority(Goal.Priority.LOW);
        newCar.setUser(user);
        goalRepository.save(newCar);

        System.out.println("✅ Sample data loaded successfully!");
        System.out.println("➡ Demo login: demo / password123");
    }
}
