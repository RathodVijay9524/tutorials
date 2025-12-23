package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    // Find all active badges
    List<Badge> findByIsActiveTrueOrderByDisplayOrderAsc();

    // Find badges by category
    List<Badge> findByCategoryAndIsActiveTrueOrderByDisplayOrderAsc(String category);

    // Find badge by name
    Optional<Badge> findByName(String name);

    // Count active badges
    long countByIsActiveTrue();
}
