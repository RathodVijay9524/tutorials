package com.vijay.User_Master.dto.tutorial;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonRatingDTO {
    
    private Long id;
    private Long lessonId;
    private String lessonTitle;
    private Long userId;
    private String userName;
    private String userImageName;
    private Integer rating; // 1-5
    private String review;
    private boolean isApproved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
