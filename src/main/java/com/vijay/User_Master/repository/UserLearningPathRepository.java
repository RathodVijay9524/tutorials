package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.UserLearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLearningPathRepository extends JpaRepository<UserLearningPath, Long> {

    // Find user's learning paths
    List<UserLearningPath> findByUserIdOrderByLastAccessedAtDesc(Long userId);

    // Find user's active (incomplete) learning paths
    List<UserLearningPath> findByUserIdAndIsCompletedFalseOrderByLastAccessedAtDesc(Long userId);

    // Find user's completed learning paths
    List<UserLearningPath> findByUserIdAndIsCompletedTrueOrderByCompletedAtDesc(Long userId);

    // Find specific user learning path
    Optional<UserLearningPath> findByUserIdAndLearningPathId(Long userId, Long learningPathId);

    // Check if user is enrolled in learning path
    boolean existsByUserIdAndLearningPathId(Long userId, Long learningPathId);

    // Get user's progress statistics
    @Query("SELECT COUNT(ulp) FROM UserLearningPath ulp WHERE ulp.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(ulp) FROM UserLearningPath ulp WHERE ulp.user.id = :userId AND ulp.isCompleted = true")
    Long countCompletedByUserId(@Param("userId") Long userId);

    // Find learning paths with low progress (for recommendations)
    @Query("SELECT ulp FROM UserLearningPath ulp " +
           "WHERE ulp.user.id = :userId AND ulp.progressPercentage < 50 AND ulp.isCompleted = false " +
           "ORDER BY ulp.lastAccessedAt DESC")
    List<UserLearningPath> findIncompleteLearningPaths(@Param("userId") Long userId);

    // Get learning path enrollment count
    long countByLearningPathId(Long learningPathId);

    // Get learning path completion count
    long countByLearningPathIdAndIsCompletedTrue(Long learningPathId);
}

