package com.vijay.User_Master.dto.tutorial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingSummaryDTO {
    private Long tutorialId;
    private Double averageRating;
    private Long totalRatings;
    private Map<Integer, Long> distribution; // Star level -> count
    private boolean userHasRated;
    private Integer userRating;
}
