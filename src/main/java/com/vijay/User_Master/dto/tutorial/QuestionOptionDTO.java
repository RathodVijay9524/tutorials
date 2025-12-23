package com.vijay.User_Master.dto.tutorial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionOptionDTO {
    private Long id;
    private String optionText;
    private Integer displayOrder;
    // Note: isCorrect is intentionally NOT included here to prevent answer leakage
    // It will only be included in response DTOs after submission
}
