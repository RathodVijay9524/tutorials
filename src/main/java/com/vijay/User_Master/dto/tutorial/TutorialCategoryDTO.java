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
public class TutorialCategoryDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String icon;
    private Integer displayOrder;
    private boolean isActive;
    private Long parentId;
    private String parentName;
    private List<TutorialCategoryDTO> subCategories;
    private Integer tutorialCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
