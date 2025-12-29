package com.vijay.User_Master.dto.tutorial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {
    private LearningPathDTO recommendedPath;
    private String reasoning; // Why this path was recommended
    private List<String> keyConcepts; // Key concepts covered
    private Integer estimatedHours;
    private String difficultyLevel;
    private Double confidenceScore; // 0.0 to 1.0 - how confident the recommendation is
}

