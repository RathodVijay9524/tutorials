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
public class LearningPathStepDTO {
    private Long id;
    private Long learningPathId;
    private Long tutorialId;
    private String tutorialTitle;
    private String tutorialSlug;
    private String tutorialDifficulty;
    private Integer tutorialEstimatedMinutes;
    private Integer stepOrder;
    private boolean isOptional;
    private String notes;
    private LocalDateTime createdAt;
    
    // User progress for this step
    private boolean isCompleted;
    private Integer progressPercentage;
}

