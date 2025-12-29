package com.vijay.User_Master.controller;

import com.vijay.User_Master.dto.tutorial.*;
import com.vijay.User_Master.exceptions.GenericResponse;
import com.vijay.User_Master.Helper.ExceptionUtil;
import com.vijay.User_Master.service.LearningPathService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learning-paths")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Learning Paths", description = "Learning path management and AI-powered recommendations")
public class LearningPathController {

    private final LearningPathService learningPathService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_USER')")
    @Operation(summary = "Create a new learning path", 
               description = "Create a learning path with ordered tutorials. Requires ADMIN or SUPER_USER role.")
    public ResponseEntity<?> createLearningPath(@Valid @RequestBody LearningPathRequest request) {
        log.info("Creating learning path: {}", request.getName());
        try {
            LearningPathDTO path = learningPathService.createLearningPath(request);
            return ExceptionUtil.createBuildResponse(path, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating learning path: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to create learning path: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping
    @Operation(summary = "Get all public learning paths", 
               description = "Retrieve all active public learning paths with optional search and filter")
    public ResponseEntity<?> getAllPublicLearningPaths(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (keyword != null || difficulty != null) {
                // Use search/filter with pagination
                Page<LearningPathDTO> paths = learningPathService.searchAndFilterLearningPaths(
                        keyword, difficulty, sortBy, sortDir, page, size);
                return ExceptionUtil.createBuildResponse(paths, HttpStatus.OK);
            } else {
                // Return all paths (backward compatibility)
                List<LearningPathDTO> paths = learningPathService.getAllPublicLearningPaths();
                return ExceptionUtil.createBuildResponse(paths, HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("Error fetching learning paths: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to fetch learning paths",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured learning paths", 
               description = "Retrieve featured learning paths")
    public ResponseEntity<?> getFeaturedLearningPaths() {
        try {
            List<LearningPathDTO> paths = learningPathService.getFeaturedLearningPaths();
            return ExceptionUtil.createBuildResponse(paths, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching featured learning paths: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to fetch featured learning paths",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/recommended")
    @Operation(summary = "Get recommended learning paths", 
               description = "Get personalized learning path recommendations based on user's progress")
    public ResponseEntity<?> getRecommendedLearningPaths() {
        try {
            List<LearningPathDTO> paths = learningPathService.getRecommendedLearningPaths();
            return ExceptionUtil.createBuildResponse(paths, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching recommended learning paths: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to fetch recommended learning paths",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get learning path by ID", 
               description = "Retrieve a specific learning path with all its steps")
    public ResponseEntity<?> getLearningPathById(@PathVariable Long id) {
        try {
            LearningPathDTO path = learningPathService.getLearningPathById(id);
            return ExceptionUtil.createBuildResponse(path, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Learning path not found: {}", id);
            return ExceptionUtil.createErrorResponseMessage(
                    e.getMessage(),
                    HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            log.error("Error fetching learning path: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to fetch learning path",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PostMapping("/{id}/enroll")
    @Operation(summary = "Enroll in a learning path", 
               description = "Enroll the current user in a learning path")
    public ResponseEntity<?> enrollInLearningPath(@PathVariable Long id) {
        try {
            UserLearningPathDTO enrollment = learningPathService.enrollInLearningPath(id);
            return ExceptionUtil.createBuildResponse(enrollment, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            log.error("Error enrolling in learning path: {}", e.getMessage());
            return ExceptionUtil.createErrorResponseMessage(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            log.error("Error enrolling in learning path: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to enroll in learning path",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @DeleteMapping("/{id}/enroll")
    @Operation(summary = "Unenroll from a learning path", 
               description = "Remove the current user's enrollment from a learning path")
    public ResponseEntity<?> unenrollFromLearningPath(@PathVariable Long id) {
        try {
            learningPathService.unenrollFromLearningPath(id);
            return ExceptionUtil.createBuildResponseMessage(
                    "Successfully unenrolled from learning path",
                    HttpStatus.OK
            );
        } catch (RuntimeException e) {
            log.error("Error unenrolling from learning path: {}", e.getMessage());
            return ExceptionUtil.createErrorResponseMessage(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            log.error("Error unenrolling from learning path: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to unenroll from learning path",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/my-paths")
    @Operation(summary = "Get user's learning paths", 
               description = "Get all learning paths the current user is enrolled in")
    public ResponseEntity<?> getUserLearningPaths() {
        try {
            List<UserLearningPathDTO> paths = learningPathService.getUserLearningPaths();
            return ExceptionUtil.createBuildResponse(paths, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching user learning paths: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to fetch user learning paths",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PostMapping("/{pathId}/progress/{tutorialId}")
    @Operation(summary = "Update learning path progress", 
               description = "Update progress when user completes a tutorial in the learning path")
    public ResponseEntity<?> updateProgress(
            @PathVariable Long pathId,
            @PathVariable Long tutorialId) {
        try {
            learningPathService.updateProgress(pathId, tutorialId);
            return ExceptionUtil.createBuildResponseMessage(
                    "Progress updated successfully",
                    HttpStatus.OK
            );
        } catch (RuntimeException e) {
            log.error("Error updating progress: {}", e.getMessage());
            return ExceptionUtil.createErrorResponseMessage(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            log.error("Error updating progress: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to update progress",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate AI-powered learning path", 
               description = "Generate a personalized learning path based on user's goal and progress")
    public ResponseEntity<?> generatePersonalizedLearningPath(
            @Valid @RequestBody RecommendationRequest request) {
        log.info("Generating personalized learning path with goal: {}", request.getGoal());
        try {
            RecommendationResponse response = learningPathService
                    .generatePersonalizedLearningPath(request);
            return ExceptionUtil.createBuildResponse(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error generating learning path: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to generate learning path: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // ============= Rating Endpoints =============

    @PostMapping("/{id}/rate")
    @Operation(summary = "Rate a learning path", 
               description = "Rate a learning path (1-5 stars) with optional review")
    public ResponseEntity<?> rateLearningPath(
            @PathVariable Long id,
            @Valid @RequestBody RatingRequest request) {
        try {
            LearningPathRatingDTO rating = learningPathService.rateLearningPath(id, request);
            return ExceptionUtil.createBuildResponse(rating, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            log.error("Error rating learning path: {}", e.getMessage());
            return ExceptionUtil.createErrorResponseMessage(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            log.error("Error rating learning path: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to rate learning path",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/{id}/ratings")
    @Operation(summary = "Get all ratings for a learning path", 
               description = "Retrieve all ratings and reviews for a learning path")
    public ResponseEntity<?> getLearningPathRatings(@PathVariable Long id) {
        try {
            List<LearningPathRatingDTO> ratings = learningPathService.getLearningPathRatings(id);
            return ExceptionUtil.createBuildResponse(ratings, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching ratings: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to fetch ratings",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/{id}/my-rating")
    @Operation(summary = "Get user's rating for a learning path", 
               description = "Get the current user's rating for a learning path")
    public ResponseEntity<?> getUserRating(@PathVariable Long id) {
        try {
            LearningPathRatingDTO rating = learningPathService.getUserRating(id);
            return ExceptionUtil.createBuildResponse(rating, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching user rating: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to fetch user rating",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @DeleteMapping("/{id}/rate")
    @Operation(summary = "Delete user's rating", 
               description = "Remove the current user's rating for a learning path")
    public ResponseEntity<?> deleteRating(@PathVariable Long id) {
        try {
            learningPathService.deleteRating(id);
            return ExceptionUtil.createBuildResponseMessage(
                    "Rating deleted successfully",
                    HttpStatus.OK
            );
        } catch (RuntimeException e) {
            log.error("Error deleting rating: {}", e.getMessage());
            return ExceptionUtil.createErrorResponseMessage(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            log.error("Error deleting rating: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to delete rating",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}

