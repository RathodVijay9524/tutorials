package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    // Find all badges for a user
    @Query("SELECT ub FROM UserBadge ub JOIN FETCH ub.badge WHERE ub.user.id = :userId ORDER BY ub.earnedAt DESC")
    List<UserBadge> findByUserIdWithBadge(@Param("userId") Long userId);

    // Check if user has a specific badge
    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);

    // Count badges for a user
    long countByUserId(Long userId);

    // Find recent badges (for notifications)
    @Query("SELECT ub FROM UserBadge ub JOIN FETCH ub.badge WHERE ub.user.id = :userId ORDER BY ub.earnedAt DESC")
    List<UserBadge> findRecentByUserId(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);
}
