package com.vijay.User_Master.controller;

import com.vijay.User_Master.dto.tutorial.LessonRatingSummaryDTO;
import com.vijay.User_Master.dto.tutorial.LessonRatingDTO;
import com.vijay.User_Master.service.LessonRatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lesson-ratings")
@RequiredArgsConstructor
@Tag(name = "Lesson Ratings", description = "Video lesson rating and review endpoints")
public class LessonRatingController {

    private final LessonRatingService ratingService;

    @Operation(summary = "Rate a lesson")
    @PostMapping("/lesson/{lessonId}")
    public ResponseEntity<LessonRatingDTO> rateLesson(
            @PathVariable Long lessonId,
            @RequestBody Map<String, Object> request) {
        Integer rating = (Integer) request.get("rating");
        String review = (String) request.get("review");
        
        LessonRatingDTO result = ratingService.rateLesson(lessonId, rating, review);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(summary = "Delete rating")
    @DeleteMapping("/lesson/{lessonId}")
    public ResponseEntity<Void> deleteRating(@PathVariable Long lessonId) {
        ratingService.deleteRating(lessonId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get rating summary for a lesson")
    @GetMapping("/lesson/{lessonId}/summary")
    public ResponseEntity<LessonRatingSummaryDTO> getRatingSummary(@PathVariable Long lessonId) {
        LessonRatingSummaryDTO summary = ratingService.getRatingSummary(lessonId);
        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Get reviews for a lesson")
    @GetMapping("/lesson/{lessonId}/reviews")
    public ResponseEntity<List<LessonRatingDTO>> getLessonReviews(@PathVariable Long lessonId) {
        List<LessonRatingDTO> reviews = ratingService.getLessonReviews(lessonId);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Get reviews with pagination")
    @GetMapping("/lesson/{lessonId}/reviews/paged")
    public ResponseEntity<Page<LessonRatingDTO>> getLessonReviewsPaged(
            @PathVariable Long lessonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<LessonRatingDTO> reviews = ratingService.getLessonReviews(lessonId, PageRequest.of(page, size));
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Get current user's rating for a lesson")
    @GetMapping("/lesson/{lessonId}/my-rating")
    public ResponseEntity<LessonRatingDTO> getUserRating(@PathVariable Long lessonId) {
        LessonRatingDTO rating = ratingService.getUserRating(lessonId);
        if (rating == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(rating);
    }

    @Operation(summary = "Get current user's rating history")
    @GetMapping("/my-ratings")
    public ResponseEntity<List<LessonRatingDTO>> getMyRatings() {
        List<LessonRatingDTO> ratings = ratingService.getMyRatings();
        return ResponseEntity.ok(ratings);
    }

    // Admin endpoints

    @Operation(summary = "Approve a rating (admin)")
    @PutMapping("/{ratingId}/approve")
    public ResponseEntity<LessonRatingDTO> approveRating(@PathVariable Long ratingId) {
        LessonRatingDTO rating = ratingService.approveRating(ratingId);
        return ResponseEntity.ok(rating);
    }

    @Operation(summary = "Get pending reviews (admin)")
    @GetMapping("/pending")
    public ResponseEntity<List<LessonRatingDTO>> getPendingReviews() {
        List<LessonRatingDTO> reviews = ratingService.getPendingReviews();
        return ResponseEntity.ok(reviews);
    }
}
