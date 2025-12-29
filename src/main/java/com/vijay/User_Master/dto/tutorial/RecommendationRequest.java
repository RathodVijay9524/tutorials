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
public class RecommendationRequest {
    private String goal; // e.g., "Build a REST API", "Master Java Collections"
    private String difficultyLevel; // BEGINNER, INTERMEDIATE, ADVANCED
    private Integer maxTutorials; // Maximum number of tutorials in the path
    private List<Long> preferredCategoryIds; // Preferred categories
    private List<Long> excludeTutorialIds; // Tutorials to exclude
    private Integer estimatedHours; // Desired learning time in hours
}

