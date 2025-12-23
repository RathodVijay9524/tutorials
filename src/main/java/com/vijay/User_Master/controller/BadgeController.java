package com.vijay.User_Master.controller;

import com.vijay.User_Master.dto.tutorial.BadgeDTO;
import com.vijay.User_Master.service.BadgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/badges")
@RequiredArgsConstructor
@Slf4j
public class BadgeController {

    private final BadgeService badgeService;

    /**
     * Get all available badges with user's earned status
     */
    @GetMapping
    public ResponseEntity<List<BadgeDTO>> getAllBadges() {
        try {
            List<BadgeDTO> badges = badgeService.getAllBadgesForUser();
            return ResponseEntity.ok(badges);
        } catch (Exception e) {
            log.error("Error fetching badges: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get only earned badges for current user
     */
    @GetMapping("/earned")
    public ResponseEntity<List<BadgeDTO>> getEarnedBadges() {
        try {
            List<BadgeDTO> badges = badgeService.getEarnedBadges();
            return ResponseEntity.ok(badges);
        } catch (Exception e) {
            log.error("Error fetching earned badges: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get user's badge count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getBadgeCount() {
        try {
            long count = badgeService.getUserBadgeCount();
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            log.error("Error fetching badge count: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Seed initial badges (for development/testing)
     */
    @PostMapping("/seed")
    public ResponseEntity<String> seedBadges() {
        try {
            badgeService.seedInitialBadges();
            return ResponseEntity.ok("Badges seeded successfully!");
        } catch (Exception e) {
            log.error("Error seeding badges: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error seeding badges: " + e.getMessage());
        }
    }
}
