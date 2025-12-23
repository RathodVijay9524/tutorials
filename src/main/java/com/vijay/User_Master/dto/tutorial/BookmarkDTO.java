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
public class BookmarkDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long tutorialId;
    private String tutorialTitle;
    private String tutorialSlug;
    private String categoryName;
    private String categorySlug;
    private String notes;
    private LocalDateTime createdAt;
}
