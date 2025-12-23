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
public class SubmitQuizRequest {
    private Long attemptId;
    // Map of questionId -> selectedOptionId
    private Map<Long, Long> answers;
}
