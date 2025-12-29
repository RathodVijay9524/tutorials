package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.LearningPath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {

    // Find all active public learning paths
    List<LearningPath> findByIsActiveTrueAndIsPublicTrueOrderByCreatedAtDesc();

    // Find featured learning paths
    List<LearningPath> findByIsFeaturedTrueAndIsActiveTrueOrderByCreatedAtDesc();

    // Find learning paths by creator
    List<LearningPath> findByCreatedByIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);

    // Find learning paths by difficulty
    List<LearningPath> findByDifficultyLevelAndIsActiveTrueAndIsPublicTrue(String difficulty);

    // Find learning paths by goal keyword
    @Query("SELECT lp FROM LearningPath lp WHERE lp.isActive = true AND lp.isPublic = true " +
           "AND (LOWER(lp.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(lp.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(lp.goal) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<LearningPath> searchByKeyword(@Param("keyword") String keyword);

    // Find AI-generated learning paths
    List<LearningPath> findByIsAiGeneratedTrueAndIsActiveTrueOrderByCreatedAtDesc();

    // Get popular learning paths (by enrollment count)
    @Query("SELECT lp FROM LearningPath lp WHERE lp.isActive = true AND lp.isPublic = true " +
           "ORDER BY lp.enrollmentCount DESC, lp.averageRating DESC")
    Page<LearningPath> findPopularLearningPaths(Pageable pageable);

    // Get learning paths with pagination
    Page<LearningPath> findByIsActiveTrueAndIsPublicTrue(Pageable pageable);

    // Count learning paths by category (through tutorials)
    @Query("SELECT COUNT(DISTINCT lp) FROM LearningPath lp " +
           "JOIN lp.steps s " +
           "JOIN s.tutorial t " +
           "WHERE t.category.id = :categoryId AND lp.isActive = true")
    Long countByCategoryId(@Param("categoryId") Long categoryId);
}

