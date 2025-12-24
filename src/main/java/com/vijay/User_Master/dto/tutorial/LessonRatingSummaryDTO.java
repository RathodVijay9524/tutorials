package com.vijay.User_Master.dto.tutorial;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonRatingSummaryDTO {
    
    private Long lessonId;
    private Double averageRating;
    private Long totalRatings;
    private Long fiveStarCount;
    private Long fourStarCount;
    private Long threeStarCount;
    private Long twoStarCount;
    private Long oneStarCount;
}
