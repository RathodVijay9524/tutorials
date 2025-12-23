package com.vijay.User_Master.controller;

import com.vijay.User_Master.dto.tutorial.RatingSummaryDTO;
import com.vijay.User_Master.dto.tutorial.TutorialRatingDTO;
import com.vijay.User_Master.service.TutorialRatingService;
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
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
@Tag(name = "Tutorial Ratings", description = "Tutorial rating and review endpoints")
public class TutorialRatingController {

    private final TutorialRatingService ratingService;

    @Operation(summary = "Rate a tutorial")
    @PostMapping("/tutorial/{tutorialId}")
    public ResponseEntity<TutorialRatingDTO> rateTutorial(
            @PathVariable Long tutorialId,
            @RequestBody Map<String, Object> request) {
        Integer rating = (Integer) request.get("rating");
        String review = (String) request.get("review");
        
        TutorialRatingDTO result = ratingService.rateTutorial(tutorialId, rating, review);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(summary = "Delete rating")
    @DeleteMapping("/tutorial/{tutorialId}")
    public ResponseEntity<Void> deleteRating(@PathVariable Long tutorialId) {
        ratingService.deleteRating(tutorialId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get rating summary for a tutorial")
    @GetMapping("/tutorial/{tutorialId}/summary")
    public ResponseEntity<RatingSummaryDTO> getRatingSummary(@PathVariable Long tutorialId) {
        RatingSummaryDTO summary = ratingService.getRatingSummary(tutorialId);
        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Get reviews for a tutorial")
    @GetMapping("/tutorial/{tutorialId}/reviews")
    public ResponseEntity<List<TutorialRatingDTO>> getTutorialReviews(@PathVariable Long tutorialId) {
        List<TutorialRatingDTO> reviews = ratingService.getTutorialReviews(tutorialId);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Get reviews with pagination")
    @GetMapping("/tutorial/{tutorialId}/reviews/paged")
    public ResponseEntity<Page<TutorialRatingDTO>> getTutorialReviewsPaged(
            @PathVariable Long tutorialId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TutorialRatingDTO> reviews = ratingService.getTutorialReviews(tutorialId, PageRequest.of(page, size));
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Get current user's rating for a tutorial")
    @GetMapping("/tutorial/{tutorialId}/my-rating")
    public ResponseEntity<TutorialRatingDTO> getUserRating(@PathVariable Long tutorialId) {
        TutorialRatingDTO rating = ratingService.getUserRating(tutorialId);
        if (rating == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(rating);
    }

    @Operation(summary = "Get current user's rating history")
    @GetMapping("/my-ratings")
    public ResponseEntity<List<TutorialRatingDTO>> getMyRatings() {
        List<TutorialRatingDTO> ratings = ratingService.getMyRatings();
        return ResponseEntity.ok(ratings);
    }

    // Admin endpoints

    @Operation(summary = "Approve a rating (admin)")
    @PutMapping("/{ratingId}/approve")
    public ResponseEntity<TutorialRatingDTO> approveRating(@PathVariable Long ratingId) {
        TutorialRatingDTO rating = ratingService.approveRating(ratingId);
        return ResponseEntity.ok(rating);
    }

    @Operation(summary = "Get pending reviews (admin)")
    @GetMapping("/pending")
    public ResponseEntity<List<TutorialRatingDTO>> getPendingReviews() {
        List<TutorialRatingDTO> reviews = ratingService.getPendingReviews();
        return ResponseEntity.ok(reviews);
    }
}
