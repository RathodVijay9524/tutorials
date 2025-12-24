package com.vijay.User_Master.service;

import com.vijay.User_Master.dto.tutorial.LessonRatingDTO;
import com.vijay.User_Master.dto.tutorial.LessonRatingSummaryDTO;
import com.vijay.User_Master.entity.VideoLesson;
import com.vijay.User_Master.entity.LessonRating;
import com.vijay.User_Master.entity.User;
import com.vijay.User_Master.repository.LessonRatingRepository;
import com.vijay.User_Master.repository.VideoLessonRepository;
import com.vijay.User_Master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonRatingService {

    private final LessonRatingRepository ratingRepository;
    private final VideoLessonRepository lessonRepository;
    private final UserRepository userRepository;

    /**
     * Rate a lesson (add or update rating)
     */
    @Transactional
    public LessonRatingDTO rateLesson(Long lessonId, Integer rating, String review) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        User user = getCurrentUser();
        VideoLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        
        // Check if user already rated
        Optional<LessonRating> existingRating = ratingRepository
                .findByUserIdAndLessonId(user.getId(), lessonId);
        
        LessonRating lessonRating;
        if (existingRating.isPresent()) {
            // Update existing rating
            lessonRating = existingRating.get();
            lessonRating.setRating(rating);
            lessonRating.setReview(review);
            log.info("User {} updated rating for lesson {}", user.getUsername(), lesson.getTitle());
        } else {
            // Create new rating
            lessonRating = LessonRating.builder()
                    .lesson(lesson)
                    .user(user)
                    .rating(rating)
                    .review(review)
                    .isApproved(true) // Auto-approve for now
                    .build();
            log.info("User {} rated lesson {} with {} stars", 
                    user.getUsername(), lesson.getTitle(), rating);
        }
        
        lessonRating = ratingRepository.save(lessonRating);
        
        return convertToDTO(lessonRating);
    }

    /**
     * Delete user's rating
     */
    @Transactional
    public void deleteRating(Long lessonId) {
        User user = getCurrentUser();
        
        if (!ratingRepository.existsByUserIdAndLessonId(user.getId(), lessonId)) {
            throw new RuntimeException("Rating not found");
        }
        
        ratingRepository.deleteByUserIdAndLessonId(user.getId(), lessonId);
        
        log.info("User {} deleted rating for lesson {}", user.getUsername(), lessonId);
    }

    /**
     * Get rating summary for a lesson
     */
    public LessonRatingSummaryDTO getRatingSummary(Long lessonId) {
        Double avgRating = ratingRepository.getAverageRating(lessonId);
        long totalRatings = ratingRepository.countByLessonIdAndIsApprovedTrue(lessonId);
        
        // Get distribution
        long fiveStar = 0, fourStar = 0, threeStar = 0, twoStar = 0, oneStar = 0;
        
        List<Object[]> distData = ratingRepository.getRatingDistribution(lessonId);
        for (Object[] row : distData) {
            Integer stars = (Integer) row[0];
            Long count = (Long) row[1];
            switch (stars) {
                case 5: fiveStar = count; break;
                case 4: fourStar = count; break;
                case 3: threeStar = count; break;
                case 2: twoStar = count; break;
                case 1: oneStar = count; break;
            }
        }
        
        return LessonRatingSummaryDTO.builder()
                .lessonId(lessonId)
                .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
                .totalRatings(totalRatings)
                .fiveStarCount(fiveStar)
                .fourStarCount(fourStar)
                .threeStarCount(threeStar)
                .twoStarCount(twoStar)
                .oneStarCount(oneStar)
                .build();
    }

    /**
     * Get all reviews for a lesson
     */
    public List<LessonRatingDTO> getLessonReviews(Long lessonId) {
        return ratingRepository.findByLessonIdAndIsApprovedTrueOrderByCreatedAtDesc(lessonId)
                .stream()
                .filter(r -> r.getReview() != null && !r.getReview().trim().isEmpty())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated reviews
     */
    public Page<LessonRatingDTO> getLessonReviews(Long lessonId, Pageable pageable) {
        return ratingRepository.findByLessonIdAndIsApprovedTrueOrderByCreatedAtDesc(lessonId, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get current user's rating for a lesson
     */
    public LessonRatingDTO getUserRating(Long lessonId) {
        User user = getCurrentUser();
        return ratingRepository.findByUserIdAndLessonId(user.getId(), lessonId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Get user's ratings history
     */
    public List<LessonRatingDTO> getMyRatings() {
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
    public LessonRatingDTO approveRating(Long ratingId) {
        LessonRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating not found"));
        rating.setApproved(true);
        rating = ratingRepository.save(rating);
        
        return convertToDTO(rating);
    }

    /**
     * Get pending reviews (admin)
     */
    public List<LessonRatingDTO> getPendingReviews() {
        return ratingRepository.findByIsApprovedFalseOrderByCreatedAtAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Helper methods
    
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private LessonRatingDTO convertToDTO(LessonRating rating) {
        return LessonRatingDTO.builder()
                .id(rating.getId())
                .lessonId(rating.getLesson().getId())
                .lessonTitle(rating.getLesson().getTitle())
                .userId(rating.getUser().getId())
                .userName(rating.getUser().getName())
                .userImageName(rating.getUser().getImageName())
                .rating(rating.getRating())
                .review(rating.getReview())
                .isApproved(rating.isApproved())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}
