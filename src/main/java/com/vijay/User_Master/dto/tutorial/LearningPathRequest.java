package com.vijay.User_Master.dto.tutorial;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathRequest {
    
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must be less than 200 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;
    
    @Size(max = 500, message = "Goal must be less than 500 characters")
    private String goal;
    
    @Builder.Default
    private boolean isPublic = true;
    
    @Builder.Default
    private boolean isFeatured = false;
    private String difficultyLevel; // BEGINNER, INTERMEDIATE, ADVANCED
    
    @NotNull(message = "Tutorial IDs are required")
    @Size(min = 1, message = "At least one tutorial is required")
    private List<Long> tutorialIds; // Ordered list of tutorial IDs
}

