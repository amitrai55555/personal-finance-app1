package com.finance.controller;

import com.finance.dto.GoalRequest;
import com.finance.entity.Goal;
import com.finance.entity.Goal.GoalStatus;
import com.finance.entity.Goal.Priority;
import com.finance.security.UserPrincipal;
import com.finance.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GoalController {
    
    @Autowired
    private GoalService goalService;
    
    @PostMapping
    public ResponseEntity<?> createGoal(@Valid @RequestBody GoalRequest request, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Goal goal = goalService.createGoal(request, userPrincipal.getId());
            return ResponseEntity.ok(goal);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Goal>> getAllGoals(Authentication authentication,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        if (page == 0 && size == 10) {
            // Return all goals if no pagination specified
            List<Goal> goals = goalService.getAllGoalsByUserId(userPrincipal.getId());
            return ResponseEntity.ok(goals);
        } else {
            // Return paginated results
            Pageable pageable = PageRequest.of(page, size);
            Page<Goal> goalPage = goalService.getGoalsByUserId(userPrincipal.getId(), pageable);
            return ResponseEntity.ok(goalPage.getContent());
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Goal>> getActiveGoals(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<Goal> activeGoals = goalService.getActiveGoalsByUserId(userPrincipal.getId());
        return ResponseEntity.ok(activeGoals);
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<List<Goal>> getOverdueGoals(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<Goal> overdueGoals = goalService.getOverdueGoals(userPrincipal.getId());
        return ResponseEntity.ok(overdueGoals);
    }
    
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Goal>> getGoalsByPriority(@PathVariable Priority priority, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<Goal> goals = goalService.getGoalsByPriority(userPrincipal.getId(), priority);
        return ResponseEntity.ok(goals);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getGoalById(@PathVariable Long id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return goalService.getGoalById(id, userPrincipal.getId())
                .map(goal -> ResponseEntity.ok(goal))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGoal(@PathVariable Long id, 
                                       @Valid @RequestBody GoalRequest request, 
                                       Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Goal updatedGoal = goalService.updateGoal(id, request, userPrincipal.getId());
            return ResponseEntity.ok(updatedGoal);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PatchMapping("/{id}/progress")
    public ResponseEntity<?> updateGoalProgress(@PathVariable Long id, 
                                               @RequestBody Map<String, BigDecimal> request, 
                                               Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            BigDecimal amount = request.get("amount");
            Goal updatedGoal = goalService.updateGoalProgress(id, amount, userPrincipal.getId());
            return ResponseEntity.ok(updatedGoal);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PatchMapping("/{id}/add-progress")
    public ResponseEntity<?> addToGoalProgress(@PathVariable Long id, 
                                              @RequestBody Map<String, BigDecimal> request, 
                                              Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            BigDecimal amount = request.get("amount");
            Goal updatedGoal = goalService.addToGoalProgress(id, amount, userPrincipal.getId());
            return ResponseEntity.ok(updatedGoal);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateGoalStatus(@PathVariable Long id, 
                                             @RequestBody Map<String, GoalStatus> request, 
                                             Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            GoalStatus status = request.get("status");
            Goal updatedGoal = goalService.updateGoalStatus(id, status, userPrincipal.getId());
            return ResponseEntity.ok(updatedGoal);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable Long id, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            goalService.deleteGoal(id, userPrincipal.getId());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Goal deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGoalStats(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGoals", goalService.getGoalCountByStatus(userPrincipal.getId(), null));
        stats.put("activeGoals", goalService.getGoalCountByStatus(userPrincipal.getId(), GoalStatus.ACTIVE));
        stats.put("completedGoals", goalService.getGoalCountByStatus(userPrincipal.getId(), GoalStatus.COMPLETED));
        stats.put("averageProgress", goalService.getAverageGoalProgress(userPrincipal.getId()));
        stats.put("overdueGoals", goalService.getOverdueGoals(userPrincipal.getId()).size());
        
        return ResponseEntity.ok(stats);
    }
}
