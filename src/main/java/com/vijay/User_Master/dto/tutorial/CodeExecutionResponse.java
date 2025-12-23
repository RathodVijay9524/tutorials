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
public class CodeExecutionResponse {
    private String status; // SUCCESS, ERROR, TIMEOUT, COMPILATION_ERROR
    private String output;
    private String error;
    private Integer executionTimeMs;
    private Integer memoryUsedKb;
    private String compileOutput;
    private LocalDateTime executedAt;
    private String message;
}
