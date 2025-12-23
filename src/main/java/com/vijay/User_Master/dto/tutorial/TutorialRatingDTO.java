package com.vijay.User_Master.dto.tutorial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorialRatingDTO {
    private Long id;
    private Long tutorialId;
    private String tutorialTitle;
    private Long userId;
    private String username;
    private String userImage;
    private Integer rating;
    private String review;
    private boolean isApproved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
