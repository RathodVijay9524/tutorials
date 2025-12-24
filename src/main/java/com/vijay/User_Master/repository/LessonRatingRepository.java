package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.LessonRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRatingRepository extends JpaRepository<LessonRating, Long> {

    // Find rating by user and lesson
    Optional<LessonRating> findByUserIdAndLessonId(Long userId, Long lessonId);

    // Check if user has rated a lesson
    boolean existsByUserIdAndLessonId(Long userId, Long lessonId);

    // Get all approved ratings for a lesson
    List<LessonRating> findByLessonIdAndIsApprovedTrueOrderByCreatedAtDesc(Long lessonId);
    
    Page<LessonRating> findByLessonIdAndIsApprovedTrueOrderByCreatedAtDesc(Long lessonId, Pageable pageable);

    // Get all ratings for a lesson (admin)
    List<LessonRating> findByLessonIdOrderByCreatedAtDesc(Long lessonId);

    // Get user's ratings
    List<LessonRating> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Calculate average rating for a lesson
    @Query("SELECT AVG(r.rating) FROM LessonRating r WHERE r.lesson.id = :lessonId AND r.isApproved = true")
    Double getAverageRating(@Param("lessonId") Long lessonId);

    // Count approved ratings for a lesson
    long countByLessonIdAndIsApprovedTrue(Long lessonId);

    // Count total ratings for a lesson
    long countByLessonId(Long lessonId);

    // Get rating distribution for a lesson
    @Query("SELECT r.rating, COUNT(r) FROM LessonRating r WHERE r.lesson.id = :lessonId AND r.isApproved = true GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistribution(@Param("lessonId") Long lessonId);

    // Delete user's rating
    void deleteByUserIdAndLessonId(Long userId, Long lessonId);

    // Find pending reviews (for moderation)
    List<LessonRating> findByIsApprovedFalseOrderByCreatedAtAsc();
}
