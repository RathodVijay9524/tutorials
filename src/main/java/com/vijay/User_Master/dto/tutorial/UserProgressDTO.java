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
public class UserProgressDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long tutorialId;
    private String tutorialTitle;
    private boolean isCompleted;
    private Integer progressPercentage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer timeSpentMinutes;
    private LocalDateTime lastAccessedAt;
}
