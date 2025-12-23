package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    Optional<UserProgress> findByUserIdAndTutorialId(Long userId, Long tutorialId);

    List<UserProgress> findByUserId(Long userId);

    List<UserProgress> findByUserIdAndIsCompletedTrue(Long userId);

    List<UserProgress> findByUserIdAndIsCompletedFalse(Long userId);

    @Query("SELECT up FROM UserProgress up WHERE up.user.id = :userId " +
           "ORDER BY up.lastAccessedAt DESC")
    List<UserProgress> findRecentProgressByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.user.id = :userId AND up.isCompleted = true")
    Long countCompletedTutorialsByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(up.timeSpentMinutes) FROM UserProgress up WHERE up.user.id = :userId")
    Long getTotalTimeSpentByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(up.progressPercentage) FROM UserProgress up WHERE up.user.id = :userId")
    Double getAverageProgressByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndTutorialId(Long userId, Long tutorialId);

    // Count completed tutorials for badge system
    long countByUserIdAndIsCompletedTrue(Long userId);
}
