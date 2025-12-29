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
public class VideoProgressDTO {
    private Long id;
    private Long userId;
    private Long videoLessonId;
    private Integer watchTimeSeconds;
    private Integer lastPositionSeconds;
    private boolean isCompleted;
    private Integer completionPercentage;
    private LocalDateTime lastWatchedAt;
    private LocalDateTime completedAt;
}

