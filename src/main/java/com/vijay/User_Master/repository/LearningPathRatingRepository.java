package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.LearningPathRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningPathRatingRepository extends JpaRepository<LearningPathRating, Long> {

    // Find rating by user and learning path
    Optional<LearningPathRating> findByUserIdAndLearningPathId(Long userId, Long learningPathId);

    // Find all ratings for a learning path
    List<LearningPathRating> findByLearningPathIdOrderByCreatedAtDesc(Long learningPathId);

    // Count ratings for a learning path
    long countByLearningPathId(Long learningPathId);

    // Calculate average rating for a learning path
    @Query("SELECT AVG(lpr.rating) FROM LearningPathRating lpr WHERE lpr.learningPath.id = :learningPathId")
    Double calculateAverageRating(@Param("learningPathId") Long learningPathId);

    // Check if user has rated a learning path
    boolean existsByUserIdAndLearningPathId(Long userId, Long learningPathId);
}

