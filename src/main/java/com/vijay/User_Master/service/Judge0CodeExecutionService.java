package com.vijay.User_Master.service;

import com.vijay.User_Master.dto.tutorial.CodeExecutionRequest;
import com.vijay.User_Master.dto.tutorial.CodeExecutionResponse;
import com.vijay.User_Master.entity.CodeExecutionLog;
import com.vijay.User_Master.entity.User;
import com.vijay.User_Master.repository.CodeExecutionLogRepository;
import com.vijay.User_Master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class Judge0CodeExecutionService {

    private final CodeExecutionLogRepository executionLogRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${judge0.api.url:https://judge0-ce.p.rapidapi.com}")
    private String judge0ApiUrl;

    @Value("${judge0.api.key:}")
    private String judge0ApiKey;

    @Value("${judge0.api.host:judge0-ce.p.rapidapi.com}")
    private String judge0ApiHost;

    // Java language ID in Judge0
    private static final int JAVA_LANGUAGE_ID = 62;

    public CodeExecutionResponse executeCode(CodeExecutionRequest request, String ipAddress) {
        try {
            log.info("Executing Java code via Judge0 API");

            // Step 1: Submit code to Judge0
            String token = submitCodeToJudge0(request);
            
            if (token == null) {
                return buildErrorResponse("Failed to submit code to Judge0");
            }

            // Step 2: Poll for result (with timeout)
            CodeExecutionResponse response = pollForResult(token, 10); // 10 second timeout

            // Step 3: Save execution log
            saveExecutionLog(request, response, token, ipAddress);

            return response;

        } catch (Exception e) {
            log.error("Error executing code: {}", e.getMessage(), e);
            return buildErrorResponse("Execution failed: " + e.getMessage());
        }
    }

    private String submitCodeToJudge0(CodeExecutionRequest request) {
        try {
            // Validate code is not null or empty
            if (request.getCode() == null || request.getCode().trim().isEmpty()) {
                log.error("Code is null or empty in request");
                return null;
            }

            // Determine if using RapidAPI
            boolean isRapidAPI = judge0ApiKey != null && !judge0ApiKey.isEmpty();
            
            // Build URL with appropriate parameters
            String url = judge0ApiUrl + "/submissions?wait=false";
            if (isRapidAPI) {
                url += "&base64_encoded=true";
            }
            
            log.info("Submitting to Judge0 URL: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Add RapidAPI headers if using RapidAPI
            if (isRapidAPI) {
                headers.set("X-RapidAPI-Key", judge0ApiKey);
                headers.set("X-RapidAPI-Host", judge0ApiHost);
                log.info("Using RapidAPI with host: {}", judge0ApiHost);
            } else {
                log.info("Using local Judge0 instance (no API key)");
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("language_id", JAVA_LANGUAGE_ID);
            
            // Use base64 encoding for RapidAPI, plain text for local Judge0
            if (isRapidAPI) {
                String encodedCode = encodeBase64(request.getCode());
                requestBody.put("source_code", encodedCode);
                log.debug("Using base64 encoded code, length: {}", encodedCode.length());
            } else {
                requestBody.put("source_code", request.getCode());
                log.debug("Using plain text code, length: {}", request.getCode().length());
            }
            
            if (request.getStdin() != null && !request.getStdin().isEmpty()) {
                if (isRapidAPI) {
                    requestBody.put("stdin", encodeBase64(request.getStdin()));
                } else {
                    requestBody.put("stdin", request.getStdin());
                }
            }

            // Set limits
            requestBody.put("cpu_time_limit", request.getTimeLimit() != null ? request.getTimeLimit() : 5);
            requestBody.put("memory_limit", request.getMemoryLimit() != null ? request.getMemoryLimit() : 256000);

            log.info("Request body: language_id={}, source_code_length={}, cpu_time_limit={}, memory_limit={}", 
                    requestBody.get("language_id"), 
                    request.getCode().length(),
                    requestBody.get("cpu_time_limit"),
                    requestBody.get("memory_limit"));
            
            // Log the actual request body for debugging
            try {
                ObjectMapper mapper = new ObjectMapper();
                String jsonBody = mapper.writeValueAsString(requestBody);
                log.info("JSON request body: {}", jsonBody);
            } catch (Exception e) {
                log.error("Failed to serialize request body for logging", e);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("Judge0 response status: {}", response.getStatusCode());
            if (response.getBody() != null) {
                log.debug("Judge0 response body: {}", response.getBody());
            }

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("token");
            }

            return null;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("HTTP Client Error submitting to Judge0: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            log.error("Error details: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error submitting code to Judge0: {}", e.getMessage(), e);
            return null;
        }
    }

    private CodeExecutionResponse pollForResult(String token, int timeoutSeconds) {
        try {
            String url = judge0ApiUrl + "/submissions/" + token + "?base64_encoded=true";

            HttpHeaders headers = new HttpHeaders();
            if (judge0ApiKey != null && !judge0ApiKey.isEmpty()) {
                headers.set("X-RapidAPI-Key", judge0ApiKey);
                headers.set("X-RapidAPI-Host", judge0ApiHost);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);

            int attempts = 0;
            int maxAttempts = timeoutSeconds * 2; // Poll every 500ms

            while (attempts < maxAttempts) {
                ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> result = response.getBody();
                    Map<String, Object> status = (Map<String, Object>) result.get("status");
                    
                    int statusId = (int) status.get("id");

                    // Status IDs: 1=In Queue, 2=Processing, 3=Accepted, 4=Wrong Answer, 5=Time Limit Exceeded, 6=Compilation Error, etc.
                    if (statusId > 2) { // Completed
                        return buildResponseFromJudge0Result(result);
                    }
                }

                Thread.sleep(500); // Wait 500ms before next poll
                attempts++;
            }

            return buildErrorResponse("Execution timeout");

        } catch (Exception e) {
            log.error("Error polling Judge0 result: {}", e.getMessage(), e);
            return buildErrorResponse("Failed to get execution result");
        }
    }

    private CodeExecutionResponse buildResponseFromJudge0Result(Map<String, Object> result) {
        Map<String, Object> status = (Map<String, Object>) result.get("status");
        int statusId = (int) status.get("id");
        String statusDescription = (String) status.get("description");

        CodeExecutionResponse response = new CodeExecutionResponse();
        response.setExecutedAt(LocalDateTime.now());

        // Decode base64 outputs
        String stdout = result.get("stdout") != null ? decodeBase64((String) result.get("stdout")) : "";
        String stderr = result.get("stderr") != null ? decodeBase64((String) result.get("stderr")) : "";
        String compileOutput = result.get("compile_output") != null ? decodeBase64((String) result.get("compile_output")) : "";

        response.setOutput(stdout);
        response.setError(stderr);
        response.setCompileOutput(compileOutput);

        // Parse execution metrics
        if (result.get("time") != null) {
            String timeStr = result.get("time").toString();
            response.setExecutionTimeMs((int) (Double.parseDouble(timeStr) * 1000));
        }

        if (result.get("memory") != null) {
            response.setMemoryUsedKb((Integer) result.get("memory"));
        }

        // Determine status
        switch (statusId) {
            case 3: // Accepted
                response.setStatus("SUCCESS");
                response.setMessage("Code executed successfully");
                break;
            case 6: // Compilation Error
                response.setStatus("COMPILATION_ERROR");
                response.setMessage("Compilation failed");
                response.setError(compileOutput);
                break;
            case 5: // Time Limit Exceeded
                response.setStatus("TIMEOUT");
                response.setMessage("Execution time limit exceeded");
                break;
            case 11: // Runtime Error
            case 12: // Runtime Error
                response.setStatus("ERROR");
                response.setMessage("Runtime error occurred");
                break;
            default:
                response.setStatus("ERROR");
                response.setMessage(statusDescription);
        }

        return response;
    }

    private void saveExecutionLog(CodeExecutionRequest request, CodeExecutionResponse response, String token, String ipAddress) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username);

            CodeExecutionLog executionLog = CodeExecutionLog.builder()
                    .user(user)
                    .code(request.getCode())
                    .output(response.getOutput())
                    .error(response.getError())
                    .status(response.getStatus())
                    .executionTimeMs(response.getExecutionTimeMs())
                    .memoryUsedKb(response.getMemoryUsedKb())
                    .language(request.getLanguage() != null ? request.getLanguage() : "java")
                    .judge0Token(token)
                    .ipAddress(ipAddress)
                    .build();

            executionLogRepository.save(executionLog);
            log.info("Execution log saved for user: {}", username);

        } catch (Exception e) {
            log.error("Error saving execution log: {}", e.getMessage(), e);
        }
    }

    private CodeExecutionResponse buildErrorResponse(String message) {
        return CodeExecutionResponse.builder()
                .status("ERROR")
                .message(message)
                .executedAt(LocalDateTime.now())
                .build();
    }

    private String encodeBase64(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes());
    }

    private String decodeBase64(String encoded) {
        try {
            return new String(Base64.getDecoder().decode(encoded));
        } catch (Exception e) {
            return encoded;
        }
    }
}
