package com.finance.repository;

import com.finance.entity.Income;
import com.finance.entity.Income.IncomeCategory;
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
public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findByUserIdOrderByDateDesc(Long userId);
    Page<Income> findByUserId(Long userId, Pageable pageable);
    Page<Income> findByUserIdOrderByDateDesc(Long userId, Pageable pageable);

    List<Income> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate startDate, LocalDate endDate);

    List<Income> findByUserIdAndCategoryOrderByDateDesc(Long userId, IncomeCategory category);
    List<Income> findByUserIdAndBankAccountIdOrderByDateDesc(Long userId, Long bankAccountId);

    java.util.Optional<Income> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.user.id = :userId")
    BigDecimal getTotalIncomeByUserId(@Param("userId") Long userId);
    
    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.user.id = :userId AND i.date BETWEEN :startDate AND :endDate")
    BigDecimal getTotalIncomeByUserIdAndDateRange(@Param("userId") Long userId, 
                                                 @Param("startDate") LocalDate startDate, 
                                                 @Param("endDate") LocalDate endDate);
    
    @Query("SELECT i.category, SUM(i.amount) FROM Income i WHERE i.user.id = :userId GROUP BY i.category")
    List<Object[]> getIncomeByCategoryForUser(@Param("userId") Long userId);
    
    @Query("SELECT i.category, SUM(i.amount) FROM Income i WHERE i.user.id = :userId AND i.date BETWEEN :startDate AND :endDate GROUP BY i.category")
    List<Object[]> getIncomeByCategoryForUserAndDateRange(@Param("userId") Long userId, 
                                                         @Param("startDate") LocalDate startDate, 
                                                         @Param("endDate") LocalDate endDate);
    
    List<Income> findByUserIdAndIsRecurringTrueAndNextOccurrenceLessThanEqual(Long userId, LocalDate date);

}
