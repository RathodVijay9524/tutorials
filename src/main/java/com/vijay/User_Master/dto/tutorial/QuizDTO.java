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
public class QuizDTO {
    private Long id;
    private Long tutorialId;
    private String tutorialTitle;
    private String title;
    private String description;
    private Integer passingScore;
    private Integer timeLimitMinutes;
    private boolean isActive;
    private int questionCount;
    private int totalPoints;
    private List<QuestionDTO> questions;
    private LocalDateTime createdAt;
}
