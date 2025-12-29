package com.vijay.User_Master.controller;

import com.vijay.User_Master.dto.tutorial.QuizAttemptDTO;
import com.vijay.User_Master.dto.tutorial.QuizDTO;
import com.vijay.User_Master.dto.tutorial.SubmitQuizRequest;
import com.vijay.User_Master.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@Slf4j
public class QuizController {

    private final QuizService quizService;

    /**
     * Get quiz for a tutorial (without correct answers)
     */
    @GetMapping("/tutorial/{tutorialId}")
    public ResponseEntity<QuizDTO> getQuizForTutorial(@PathVariable Long tutorialId) {
        try {
            log.info("Fetching quiz for tutorial ID: {}", tutorialId);
            QuizDTO quiz = quizService.getQuizForTutorial(tutorialId);
            if (quiz == null) {
                log.info("No quiz found for tutorial ID: {}", tutorialId);
                return ResponseEntity.notFound().build();
            }
            log.info("Quiz found: {} with {} questions", quiz.getTitle(), quiz.getQuestionCount());
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            log.error("Error fetching quiz for tutorial {}: {}", tutorialId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get quiz by ID
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<QuizDTO> getQuizById(@PathVariable Long quizId) {
        QuizDTO quiz = quizService.getQuizById(quizId);
        return ResponseEntity.ok(quiz);
    }

    /**
     * Start a new quiz attempt
     */
    @PostMapping("/{quizId}/start")
    public ResponseEntity<QuizAttemptDTO> startQuizAttempt(@PathVariable Long quizId) {
        QuizAttemptDTO attempt = quizService.startQuizAttempt(quizId);
        return ResponseEntity.ok(attempt);
    }

    /**
     * Submit quiz answers
     */
    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<QuizAttemptDTO> submitQuiz(
            @PathVariable Long attemptId,
            @RequestBody Map<String, Long> answers) {
        
        try {
            log.info("Submitting quiz attempt {} with {} answers", attemptId, answers.size());
            SubmitQuizRequest request = SubmitQuizRequest.builder()
                    .attemptId(attemptId)
                    .answers(answers.entrySet().stream()
                            .collect(java.util.stream.Collectors.toMap(
                                    e -> Long.parseLong(e.getKey()),
                                    Map.Entry::getValue)))
                    .build();
            
            QuizAttemptDTO result = quizService.submitQuiz(request);
            log.info("Quiz submission successful for attempt {}", attemptId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error submitting quiz attempt {}: {}", attemptId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get attempt result with detailed feedback
     */
    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<QuizAttemptDTO> getAttemptResult(@PathVariable Long attemptId) {
        QuizAttemptDTO result = quizService.getAttemptResult(attemptId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get user's quiz history
     */
    @GetMapping("/{quizId}/history")
    public ResponseEntity<List<QuizAttemptDTO>> getUserQuizHistory(@PathVariable Long quizId) {
        List<QuizAttemptDTO> history = quizService.getUserQuizHistory(quizId);
        return ResponseEntity.ok(history);
    }

    /**
     * Get user's best attempt
     */
    @GetMapping("/{quizId}/best")
    public ResponseEntity<QuizAttemptDTO> getBestAttempt(@PathVariable Long quizId) {
        QuizAttemptDTO best = quizService.getBestAttempt(quizId);
        if (best == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(best);
    }

    /**
     * Update quiz attempt time remaining
     */
    @PutMapping("/attempts/{attemptId}/time")
    public ResponseEntity<QuizAttemptDTO> updateTimeRemaining(
            @PathVariable Long attemptId,
            @RequestParam Integer timeRemainingSeconds) {
        try {
            QuizAttemptDTO updated = quizService.updateTimeRemaining(attemptId, timeRemainingSeconds);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating time: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Temporary seed endpoint for testing
     */
    @PostMapping("/seed")
    public ResponseEntity<String> seedSampleQuiz() {
        try {
            quizService.seedSampleQuiz();
            return ResponseEntity.ok("Sample quiz seeded successfully!");
        } catch (Exception e) {
            log.error("Error seeding quiz: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error seeding quiz: " + e.getMessage());
        }
    }
}
