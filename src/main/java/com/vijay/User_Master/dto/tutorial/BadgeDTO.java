package com.vijay.User_Master.dto.tutorial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeDTO {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private String iconEmoji;
    private String category;
    private Integer requiredCount;
    private Integer displayOrder;
    
    // For user context
    private boolean earned;
    private String earnedAt;
    private String earnedContext;
    private Integer userProgress; // Current count towards badge
}
