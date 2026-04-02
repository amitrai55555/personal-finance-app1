package com.finance.repository;

import com.finance.entity.Goal;
import com.finance.entity.Goal.GoalStatus;
import com.finance.entity.Goal.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    
    List<Goal> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Page<Goal> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<Goal> findByUserIdAndStatusOrderByPriorityAscTargetDateAsc(Long userId, GoalStatus status);
    
    List<Goal> findByUserIdAndPriorityOrderByTargetDateAsc(Long userId, Priority priority);
    
    @Query("SELECT g FROM Goal g WHERE g.user.id = :userId AND g.targetDate < :currentDate AND g.status = 'ACTIVE'")
    List<Goal> findOverdueGoalsByUserId(@Param("userId") Long userId, @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT COUNT(g) FROM Goal g WHERE g.user.id = :userId AND g.status = :status")
    Long countGoalsByUserIdAndStatus(@Param("userId") Long userId, @Param("status") GoalStatus status);
    
    @Query("SELECT g FROM Goal g WHERE g.user.id = :userId AND g.status = 'ACTIVE' ORDER BY g.priority ASC, g.targetDate ASC")
    List<Goal> findActiveGoalsByUserIdOrderByPriorityAndDate(@Param("userId") Long userId);
    
    @Query("SELECT AVG((g.currentAmount * 100.0) / g.targetAmount) FROM Goal g WHERE g.user.id = :userId AND g.status = 'ACTIVE'")
    Double getAverageGoalProgressByUserId(@Param("userId") Long userId);
}
