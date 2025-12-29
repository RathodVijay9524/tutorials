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
public class QuizAttemptDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long quizId;
    private String quizTitle;
    private Integer score;
    private Integer maxScore;
    private Double percentage;
    private boolean isPassed;
    private boolean isCompleted;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer timeLimitSeconds;
    private Integer timeRemainingSeconds;
    private Integer timeSpentSeconds;
    private List<QuizResponseDTO> responses;
}
