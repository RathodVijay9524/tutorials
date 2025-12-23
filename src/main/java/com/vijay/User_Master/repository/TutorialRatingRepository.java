package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.TutorialRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutorialRatingRepository extends JpaRepository<TutorialRating, Long> {

    // Find rating by user and tutorial
    Optional<TutorialRating> findByUserIdAndTutorialId(Long userId, Long tutorialId);

    // Check if user has rated a tutorial
    boolean existsByUserIdAndTutorialId(Long userId, Long tutorialId);

    // Get all approved ratings for a tutorial
    List<TutorialRating> findByTutorialIdAndIsApprovedTrueOrderByCreatedAtDesc(Long tutorialId);
    
    Page<TutorialRating> findByTutorialIdAndIsApprovedTrueOrderByCreatedAtDesc(Long tutorialId, Pageable pageable);

    // Get all ratings for a tutorial (admin)
    List<TutorialRating> findByTutorialIdOrderByCreatedAtDesc(Long tutorialId);

    // Get user's ratings
    List<TutorialRating> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Calculate average rating for a tutorial
    @Query("SELECT AVG(r.rating) FROM TutorialRating r WHERE r.tutorial.id = :tutorialId AND r.isApproved = true")
    Double getAverageRating(@Param("tutorialId") Long tutorialId);

    // Count approved ratings for a tutorial
    long countByTutorialIdAndIsApprovedTrue(Long tutorialId);

    // Count total ratings for a tutorial
    long countByTutorialId(Long tutorialId);

    // Get rating distribution for a tutorial
    @Query("SELECT r.rating, COUNT(r) FROM TutorialRating r WHERE r.tutorial.id = :tutorialId AND r.isApproved = true GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistribution(@Param("tutorialId") Long tutorialId);

    // Delete user's rating
    void deleteByUserIdAndTutorialId(Long userId, Long tutorialId);

    // Find pending reviews (for moderation)
    List<TutorialRating> findByIsApprovedFalseOrderByCreatedAtAsc();
}
