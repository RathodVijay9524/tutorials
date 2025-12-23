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
public class TutorialDTO {
    private Long id;
    private String title;
    private String slug;
    private String content;
    private String codeExample;
    private String difficulty;
    private Integer estimatedMinutes;
    private Integer displayOrder;
    private Long categoryId;
    private String categoryName;
    private Long authorId;
    private String authorName;
    private boolean isPublished;
    private LocalDateTime publishedAt;
    private Long viewCount;
    private Double averageRating;
    private Integer ratingCount;
    private String videoUrl;
    private Integer videoDuration;
    private String videoThumbnail;
    private String metaTitle;
    private String metaDescription;
    private String keywords;
    private List<CodeSnippetDTO> codeSnippets;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
