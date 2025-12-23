package com.vijay.User_Master.controller;

import com.vijay.User_Master.dto.tutorial.CodeExecutionRequest;
import com.vijay.User_Master.dto.tutorial.CodeExecutionResponse;
import com.vijay.User_Master.service.Judge0CodeExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/code")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Code Execution", description = "Java code execution APIs")
public class CodeExecutionController {

    private final Judge0CodeExecutionService codeExecutionService;

    @PostMapping("/execute")
    @Operation(summary = "Execute Java code", description = "Execute Java code and return output/errors")
    public ResponseEntity<CodeExecutionResponse> executeCode(
            @Valid @RequestBody CodeExecutionRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = getClientIpAddress(httpRequest);
        CodeExecutionResponse response = codeExecutionService.executeCode(request, ipAddress);
        
        return ResponseEntity.ok(response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
