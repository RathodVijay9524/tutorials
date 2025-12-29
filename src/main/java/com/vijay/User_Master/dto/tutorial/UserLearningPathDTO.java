package com.vijay.User_Master.dto.tutorial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLearningPathDTO {
    private Long id;
    private Long userId;
    private Long learningPathId;
    private String learningPathName;
    private Integer progressPercentage;
    private Integer completedSteps;
    private Integer totalSteps;
    private boolean isCompleted;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime estimatedCompletionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

