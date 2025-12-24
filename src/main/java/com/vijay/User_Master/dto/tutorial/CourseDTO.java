package com.vijay.User_Master.dto.tutorial;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private String thumbnailUrl;
    private String difficulty;
    private boolean published;
    private Long categoryId;
    private String categoryName;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdAt;
    private List<VideoLessonDTO> lessons;
}
