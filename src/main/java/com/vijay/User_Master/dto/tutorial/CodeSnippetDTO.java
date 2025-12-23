package com.vijay.User_Master.dto.tutorial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeSnippetDTO {
    private Long id;
    private Long tutorialId;
    private String title;
    private String code;
    private String expectedOutput;
    private boolean isExecutable;
    private boolean isEditable;
    private Integer displayOrder;
    private String language;
}
