package com.vijay.User_Master.dto.tutorial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathDTO {
    private Long id;
    private String name;
    private String description;
    private String goal;
    private Long createdById;
    private String createdByName;
    private boolean isPublic;
    private boolean isFeatured;
    private Integer estimatedHours;
    private String difficultyLevel;
    private Integer enrollmentCount;
    private Integer completionCount;
    private Double averageRating;
    private Integer ratingCount;
    private boolean isActive;
    private boolean isAiGenerated;
    private List<LearningPathStepDTO> steps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // User-specific fields (if user is enrolled)
    private UserLearningPathDTO userProgress;
}

