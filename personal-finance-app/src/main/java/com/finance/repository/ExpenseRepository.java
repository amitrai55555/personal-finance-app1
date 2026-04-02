package com.finance.repository;

import com.finance.entity.Expense;
import com.finance.entity.Expense.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    List<Expense> findByUserIdOrderByDateDesc(Long userId);
    
    Page<Expense> findByUserIdOrderByDateDesc(Long userId, Pageable pageable);
    
    List<Expense> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate startDate, LocalDate endDate);
    
    List<Expense> findByUserIdAndCategoryOrderByDateDesc(Long userId, ExpenseCategory category);
    List<Expense> findByUserIdAndBankAccountIdOrderByDateDesc(Long userId, Long bankAccountId);


    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal getTotalExpensesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate")
    BigDecimal getTotalExpensesByUserIdAndDateRange(@Param("userId") Long userId, 
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);
    
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.user.id = :userId GROUP BY e.category")
    List<Object[]> getExpensesByCategoryForUser(@Param("userId") Long userId);
    
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate GROUP BY e.category")
    List<Object[]> getExpensesByCategoryForUserAndDateRange(@Param("userId") Long userId, 
                                                           @Param("startDate") LocalDate startDate, 
                                                           @Param("endDate") LocalDate endDate);
    
    List<Expense> findByUserIdAndIsRecurringTrueAndNextOccurrenceLessThanEqual(Long userId, LocalDate date);
    
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId ORDER BY e.date DESC LIMIT 10")
    List<Expense> findRecentExpensesByUserId(@Param("userId") Long userId);
}
