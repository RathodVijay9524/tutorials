package com.vijay.User_Master.controller;

import com.vijay.User_Master.dto.tutorial.UserProgressDTO;
import com.vijay.User_Master.service.UserProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
@Tag(name = "User Progress", description = "Learning progress tracking APIs")
public class UserProgressController {

    private final UserProgressService progressService;

    @GetMapping("/me")
    @Operation(summary = "Get my progress", description = "Retrieve current user's learning progress")
    public ResponseEntity<List<UserProgressDTO>> getMyProgress() {
        return ResponseEntity.ok(progressService.getCurrentUserProgress());
    }

    @PostMapping("/tutorial/{tutorialId}/start")
    @Operation(summary = "Start tutorial", description = "Mark a tutorial as started")
    public ResponseEntity<UserProgressDTO> startTutorial(@PathVariable Long tutorialId) {
        return ResponseEntity.ok(progressService.startTutorial(tutorialId));
    }

    @PatchMapping("/tutorial/{tutorialId}")
    @Operation(summary = "Update progress", description = "Update progress percentage for a tutorial")
    public ResponseEntity<UserProgressDTO> updateProgress(
            @PathVariable Long tutorialId,
            @RequestParam Integer percentage) {
        return ResponseEntity.ok(progressService.updateProgress(tutorialId, percentage));
    }

    @PostMapping("/tutorial/{tutorialId}/complete")
    @Operation(summary = "Complete tutorial", description = "Mark a tutorial as completed")
    public ResponseEntity<UserProgressDTO> completeTutorial(@PathVariable Long tutorialId) {
        return ResponseEntity.ok(progressService.completeTutorial(tutorialId));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get progress statistics", description = "Retrieve user's learning statistics")
    public ResponseEntity<Map<String, Object>> getProgressStats() {
        // Get current user ID from security context
        List<UserProgressDTO> progress = progressService.getCurrentUserProgress();
        
        if (progress.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "completedTutorials", 0,
                "totalTimeSpent", 0,
                "averageProgress", 0.0
            ));
        }

        Long userId = progress.get(0).getUserId();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("completedTutorials", progressService.getCompletedTutorialsCount(userId));
        stats.put("totalTimeSpent", progressService.getTotalTimeSpent(userId));
        stats.put("averageProgress", progressService.getAverageProgress(userId));
        
        return ResponseEntity.ok(stats);
    }
}
