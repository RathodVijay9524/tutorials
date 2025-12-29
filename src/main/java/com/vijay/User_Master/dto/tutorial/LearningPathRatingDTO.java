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
public class LearningPathRatingDTO {

    private Long id;
    private Long userId;
    private String username;
    private Long learningPathId;
    private Integer rating; // 1-5
    private String review;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

