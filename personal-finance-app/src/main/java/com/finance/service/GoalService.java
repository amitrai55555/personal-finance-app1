package com.finance.service;

import com.finance.dto.GoalRequest;
import com.finance.entity.Goal;
import com.finance.entity.Goal.GoalStatus;
import com.finance.entity.Goal.Priority;
import com.finance.entity.User;
import com.finance.repository.GoalRepository;
import com.finance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GoalService {
    
    @Autowired
    private GoalRepository goalRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Goal createGoal(GoalRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Goal goal = new Goal();
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setCurrentAmount(request.getCurrentAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setPriority(request.getPriority());
        goal.setUser(user);
        
        return goalRepository.save(goal);
    }
    
    public List<Goal> getAllGoalsByUserId(Long userId) {
        return goalRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public Page<Goal> getGoalsByUserId(Long userId, Pageable pageable) {
        return goalRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    public List<Goal> getActiveGoalsByUserId(Long userId) {
        return goalRepository.findByUserIdAndStatusOrderByPriorityAscTargetDateAsc(userId, GoalStatus.ACTIVE);
    }
    
    public List<Goal> getGoalsByPriority(Long userId, Priority priority) {
        return goalRepository.findByUserIdAndPriorityOrderByTargetDateAsc(userId, priority);
    }
    
    public List<Goal> getOverdueGoals(Long userId) {
        return goalRepository.findOverdueGoalsByUserId(userId, LocalDate.now());
    }
    
    public Optional<Goal> getGoalById(Long goalId, Long userId) {
        return goalRepository.findById(goalId)
                .filter(goal -> goal.getUser().getId().equals(userId));
    }
    
    public Goal updateGoal(Long goalId, GoalRequest request, Long userId) {
        Goal goal = getGoalById(goalId, userId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setCurrentAmount(request.getCurrentAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setPriority(request.getPriority());
        
        // Check if goal is completed
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
            if (goal.getCompletionDate() == null) {
                goal.setCompletionDate(LocalDate.now());
            }
        }
        
        return goalRepository.save(goal);
    }
    
    public Goal updateGoalProgress(Long goalId, BigDecimal amount, Long userId) {
        Goal goal = getGoalById(goalId, userId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        
        goal.setCurrentAmount(amount);
        
        // Check if goal is completed
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
            goal.setCompletionDate(LocalDate.now());
        }
        
        return goalRepository.save(goal);
    }
    
    public Goal addToGoalProgress(Long goalId, BigDecimal amount, Long userId) {
        Goal goal = getGoalById(goalId, userId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        
        BigDecimal newAmount = goal.getCurrentAmount().add(amount);
        goal.setCurrentAmount(newAmount);
        
        // Check if goal is completed
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
            goal.setCompletionDate(LocalDate.now());
        }
        
        return goalRepository.save(goal);
    }
    
    public Goal updateGoalStatus(Long goalId, GoalStatus status, Long userId) {
        Goal goal = getGoalById(goalId, userId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        
        goal.setStatus(status);
        
        if (status == GoalStatus.COMPLETED && goal.getCompletionDate() == null) {
            goal.setCompletionDate(LocalDate.now());
        }
        
        return goalRepository.save(goal);
    }
    
    public void deleteGoal(Long goalId, Long userId) {
        Goal goal = getGoalById(goalId, userId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        goalRepository.delete(goal);
    }
    
    public Long getGoalCountByStatus(Long userId, GoalStatus status) {
        return goalRepository.countGoalsByUserIdAndStatus(userId, status);
    }
    
    public Double getAverageGoalProgress(Long userId) {
        Double avgProgress = goalRepository.getAverageGoalProgressByUserId(userId);
        return avgProgress != null ? avgProgress : 0.0;
    }
}
