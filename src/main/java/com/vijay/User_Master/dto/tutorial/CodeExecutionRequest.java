package com.vijay.User_Master.dto.tutorial;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeExecutionRequest {
    
    @NotBlank(message = "Code cannot be empty")
    private String code;
    
    private String language; // Default: "java"
    
    private String stdin; // Standard input for the program
    
    private Integer timeLimit; // In seconds, default: 5
    
    private Integer memoryLimit; // In KB, default: 256000
}
