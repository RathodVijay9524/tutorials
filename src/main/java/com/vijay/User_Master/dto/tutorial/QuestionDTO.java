package com.vijay.User_Master.dto.tutorial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDTO {
    private Long id;
    private String questionText;
    private String questionType;
    private String codeSnippet;
    private String explanation;
    private Integer displayOrder;
    private Integer points;
    private List<QuestionOptionDTO> options;
}
