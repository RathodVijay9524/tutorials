package com.vijay.User_Master.service;

import com.vijay.User_Master.dto.tutorial.RatingSummaryDTO;
import com.vijay.User_Master.dto.tutorial.TutorialRatingDTO;
import com.vijay.User_Master.entity.Tutorial;
import com.vijay.User_Master.entity.TutorialRating;
import com.vijay.User_Master.entity.User;
import com.vijay.User_Master.repository.TutorialRatingRepository;
import com.vijay.User_Master.repository.TutorialRepository;
import com.vijay.User_Master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TutorialRatingService {

    private final TutorialRatingRepository ratingRepository;
    private final TutorialRepository tutorialRepository;
    private final UserRepository userRepository;

    /**
     * Rate a tutorial (add or update rating)
     */
    @Transactional
    public TutorialRatingDTO rateTutorial(Long tutorialId, Integer rating, String review) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        User user = getCurrentUser();
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new RuntimeException("Tutorial not found"));
        
        // Check if user already rated
        Optional<TutorialRating> existingRating = ratingRepository
                .findByUserIdAndTutorialId(user.getId(), tutorialId);
        
        TutorialRating tutorialRating;
        if (existingRating.isPresent()) {
            // Update existing rating
            tutorialRating = existingRating.get();
            tutorialRating.setRating(rating);
            tutorialRating.setReview(review);
            log.info("User {} updated rating for tutorial {}", user.getUsername(), tutorial.getTitle());
        } else {
            // Create new rating
            tutorialRating = TutorialRating.builder()
                    .tutorial(tutorial)
                    .user(user)
                    .rating(rating)
                    .review(review)
                    .isApproved(true) // Auto-approve for now
                    .build();
            log.info("User {} rated tutorial {} with {} stars", 
                    user.getUsername(), tutorial.getTitle(), rating);
        }
        
        tutorialRating = ratingRepository.save(tutorialRating);
        
        // Update tutorial's average rating
        updateTutorialRatingStats(tutorialId);
        
        return convertToDTO(tutorialRating);
    }

    /**
     * Delete user's rating
     */
    @Transactional
    public void deleteRating(Long tutorialId) {
        User user = getCurrentUser();
        
        if (!ratingRepository.existsByUserIdAndTutorialId(user.getId(), tutorialId)) {
            throw new RuntimeException("Rating not found");
        }
        
        ratingRepository.deleteByUserIdAndTutorialId(user.getId(), tutorialId);
        
        // Update tutorial stats
        updateTutorialRatingStats(tutorialId);
        
        log.info("User {} deleted rating for tutorial {}", user.getUsername(), tutorialId);
    }

    /**
     * Get rating summary for a tutorial
     */
    public RatingSummaryDTO getRatingSummary(Long tutorialId) {
        Double avgRating = ratingRepository.getAverageRating(tutorialId);
        long totalRatings = ratingRepository.countByTutorialIdAndIsApprovedTrue(tutorialId);
        
        // Get distribution
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        
        List<Object[]> distData = ratingRepository.getRatingDistribution(tutorialId);
        for (Object[] row : distData) {
            Integer stars = (Integer) row[0];
            Long count = (Long) row[1];
            distribution.put(stars, count);
        }
        
        // Check if current user has rated
        boolean userHasRated = false;
        Integer userRating = null;
        try {
            User user = getCurrentUser();
            Optional<TutorialRating> userRatingOpt = ratingRepository
                    .findByUserIdAndTutorialId(user.getId(), tutorialId);
            if (userRatingOpt.isPresent()) {
                userHasRated = true;
                userRating = userRatingOpt.get().getRating();
            }
        } catch (Exception e) {
            // User not logged in
        }
        
        return RatingSummaryDTO.builder()
                .tutorialId(tutorialId)
                .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
                .totalRatings(totalRatings)
                .distribution(distribution)
                .userHasRated(userHasRated)
                .userRating(userRating)
                .build();
    }

    /**
     * Get all reviews for a tutorial
     */
    public List<TutorialRatingDTO> getTutorialReviews(Long tutorialId) {
        return ratingRepository.findByTutorialIdAndIsApprovedTrueOrderByCreatedAtDesc(tutorialId)
                .stream()
                .filter(r -> r.getReview() != null && !r.getReview().trim().isEmpty())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated reviews
     */
    public Page<TutorialRatingDTO> getTutorialReviews(Long tutorialId, Pageable pageable) {
        return ratingRepository.findByTutorialIdAndIsApprovedTrueOrderByCreatedAtDesc(tutorialId, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get current user's rating for a tutorial
     */
    public TutorialRatingDTO getUserRating(Long tutorialId) {
        User user = getCurrentUser();
        return ratingRepository.findByUserIdAndTutorialId(user.getId(), tutorialId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Get user's ratings history
     */
    public List<TutorialRatingDTO> getMyRatings() {
        User user = getCurrentUser();
        return ratingRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Admin methods
    
    /**
     * Approve a rating (admin)
     */
    @Transactional
    public TutorialRatingDTO approveRating(Long ratingId) {
        TutorialRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating not found"));
        rating.setApproved(true);
        rating = ratingRepository.save(rating);
        
        // Update tutorial stats
        updateTutorialRatingStats(rating.getTutorial().getId());
        
        return convertToDTO(rating);
    }

    /**
     * Get pending reviews (admin)
     */
    public List<TutorialRatingDTO> getPendingReviews() {
        return ratingRepository.findByIsApprovedFalseOrderByCreatedAtAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Helper methods
    
    private void updateTutorialRatingStats(Long tutorialId) {
        Tutorial tutorial = tutorialRepository.findById(tutorialId).orElse(null);
        if (tutorial != null) {
            Double avgRating = ratingRepository.getAverageRating(tutorialId);
            long ratingCount = ratingRepository.countByTutorialIdAndIsApprovedTrue(tutorialId);
            
            // Note: You'll need to add these fields to Tutorial entity
            // tutorial.setAverageRating(avgRating != null ? avgRating : 0.0);
            // tutorial.setRatingCount((int) ratingCount);
            // tutorialRepository.save(tutorial);
            
            log.debug("Updated tutorial {} stats: avg={}, count={}", tutorialId, avgRating, ratingCount);
        }
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private TutorialRatingDTO convertToDTO(TutorialRating rating) {
        return TutorialRatingDTO.builder()
                .id(rating.getId())
                .tutorialId(rating.getTutorial().getId())
                .tutorialTitle(rating.getTutorial().getTitle())
                .userId(rating.getUser().getId())
                .username(rating.getUser().getUsername())
                .userImage(rating.getUser().getImageName())
                .rating(rating.getRating())
                .review(rating.getReview())
                .isApproved(rating.isApproved())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}
