package com.vijay.User_Master.service;

import com.vijay.User_Master.dto.tutorial.BadgeDTO;
import com.vijay.User_Master.entity.*;
import com.vijay.User_Master.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // ============ Public Badge Retrieval Methods ============

    /**
     * Get all available badges with user's earned status
     */
    @Transactional(readOnly = true)
    public List<BadgeDTO> getAllBadgesForUser() {
        User user = getCurrentUser();
        List<Badge> allBadges = badgeRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        List<UserBadge> userBadges = userBadgeRepository.findByUserIdWithBadge(user.getId());
        
        return allBadges.stream()
                .map(badge -> convertToDTO(badge, userBadges, user))
                .collect(Collectors.toList());
    }

    /**
     * Get only earned badges for a user
     */
    @Transactional(readOnly = true)
    public List<BadgeDTO> getEarnedBadges() {
        User user = getCurrentUser();
        List<UserBadge> userBadges = userBadgeRepository.findByUserIdWithBadge(user.getId());
        
        return userBadges.stream()
                .map(ub -> convertEarnedToDTO(ub))
                .collect(Collectors.toList());
    }

    /**
     * Get user's badge count
     */
    @Transactional(readOnly = true)
    public long getUserBadgeCount() {
        User user = getCurrentUser();
        return userBadgeRepository.countByUserId(user.getId());
    }

    // ============ Badge Awarding Methods ============

    /**
     * Check and award tutorial-related badges
     */
    @Transactional
    public void checkTutorialBadges(User user, Tutorial tutorial) {
        long completedCount = userProgressRepository.countByUserIdAndIsCompletedTrue(user.getId());
        
        // First Steps - Complete 1 tutorial
        if (completedCount >= 1) {
            awardBadgeByName(user, "First Steps", "Completed: " + tutorial.getTitle());
        }
        
        // Tutorial Pioneer - Complete 5 tutorials
        if (completedCount >= 5) {
            awardBadgeByName(user, "Tutorial Pioneer", "Completed 5 tutorials");
        }
        
        // Tutorial Master - Complete 10 tutorials
        if (completedCount >= 10) {
            awardBadgeByName(user, "Tutorial Master", "Completed 10 tutorials");
        }
        
        log.info("Checked tutorial badges for user {}. Completed count: {}", user.getUsername(), completedCount);
    }

    /**
     * Check and award quiz-related badges
     */
    @Transactional
    public void checkQuizBadges(User user, QuizAttempt attempt) {
        long passedCount = quizAttemptRepository.findAll().stream()
                .filter(a -> a.getUser().getId().equals(user.getId()) && a.isPassed())
                .count();
        
        // Quiz Rookie - Pass 1 quiz
        if (passedCount >= 1) {
            awardBadgeByName(user, "Quiz Rookie", "Passed: " + attempt.getQuiz().getTitle());
        }
        
        // Quiz Champion - Pass 5 quizzes
        if (passedCount >= 5) {
            awardBadgeByName(user, "Quiz Champion", "Passed 5 quizzes");
        }
        
        // Perfect Score - Score 100% on any quiz
        if (attempt.getPercentage() != null && attempt.getPercentage() >= 100.0) {
            awardBadgeByName(user, "Perfect Score", "100% on: " + attempt.getQuiz().getTitle());
        }
        
        log.info("Checked quiz badges for user {}. Passed count: {}", user.getUsername(), passedCount);
    }

    /**
     * Award Welcome badge for new users
     */
    @Transactional
    public void awardWelcomeBadge(User user) {
        awardBadgeByName(user, "Welcome", "Joined the platform");
    }

    // ============ Private Helper Methods ============

    private void awardBadgeByName(User user, String badgeName, String context) {
        badgeRepository.findByName(badgeName).ifPresent(badge -> {
            if (!userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
                UserBadge userBadge = UserBadge.builder()
                        .user(user)
                        .badge(badge)
                        .context(context)
                        .build();
                userBadgeRepository.save(userBadge);
                log.info("Awarded badge '{}' to user '{}'", badgeName, user.getUsername());
            }
        });
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private BadgeDTO convertToDTO(Badge badge, List<UserBadge> userBadges, User user) {
        UserBadge earned = userBadges.stream()
                .filter(ub -> ub.getBadge().getId().equals(badge.getId()))
                .findFirst()
                .orElse(null);
        
        Integer progress = calculateProgress(badge, user);
        
        return BadgeDTO.builder()
                .id(badge.getId())
                .name(badge.getName())
                .description(badge.getDescription())
                .iconUrl(badge.getIconUrl())
                .iconEmoji(badge.getIconEmoji())
                .category(badge.getCategory())
                .requiredCount(badge.getRequiredCount())
                .displayOrder(badge.getDisplayOrder())
                .earned(earned != null)
                .earnedAt(earned != null ? earned.getEarnedAt().format(DATE_FORMATTER) : null)
                .earnedContext(earned != null ? earned.getContext() : null)
                .userProgress(progress)
                .build();
    }

    private BadgeDTO convertEarnedToDTO(UserBadge userBadge) {
        Badge badge = userBadge.getBadge();
        return BadgeDTO.builder()
                .id(badge.getId())
                .name(badge.getName())
                .description(badge.getDescription())
                .iconUrl(badge.getIconUrl())
                .iconEmoji(badge.getIconEmoji())
                .category(badge.getCategory())
                .requiredCount(badge.getRequiredCount())
                .displayOrder(badge.getDisplayOrder())
                .earned(true)
                .earnedAt(userBadge.getEarnedAt().format(DATE_FORMATTER))
                .earnedContext(userBadge.getContext())
                .build();
    }

    private Integer calculateProgress(Badge badge, User user) {
        if (badge.getCategory() == null) return 0;
        
        switch (badge.getCategory()) {
            case "TUTORIAL":
                return (int) userProgressRepository.countByUserIdAndIsCompletedTrue(user.getId());
            case "QUIZ":
                return (int) quizAttemptRepository.findAll().stream()
                        .filter(a -> a.getUser().getId().equals(user.getId()) && a.isPassed())
                        .count();
            default:
                return 0;
        }
    }

    // ============ Seed Initial Badges ============

    @Transactional
    public void seedInitialBadges() {
        if (badgeRepository.count() > 0) {
            log.info("Badges already seeded, skipping...");
            return;
        }

        // Tutorial badges
        badgeRepository.save(Badge.builder()
                .name("First Steps").description("Complete your first tutorial")
                .iconEmoji("🎯").category("TUTORIAL").requiredCount(1).displayOrder(1).build());
        
        badgeRepository.save(Badge.builder()
                .name("Tutorial Pioneer").description("Complete 5 tutorials")
                .iconEmoji("📚").category("TUTORIAL").requiredCount(5).displayOrder(2).build());
        
        badgeRepository.save(Badge.builder()
                .name("Tutorial Master").description("Complete 10 tutorials")
                .iconEmoji("🏆").category("TUTORIAL").requiredCount(10).displayOrder(3).build());

        // Quiz badges
        badgeRepository.save(Badge.builder()
                .name("Quiz Rookie").description("Pass your first quiz")
                .iconEmoji("✅").category("QUIZ").requiredCount(1).displayOrder(4).build());
        
        badgeRepository.save(Badge.builder()
                .name("Quiz Champion").description("Pass 5 quizzes")
                .iconEmoji("🌟").category("QUIZ").requiredCount(5).displayOrder(5).build());
        
        badgeRepository.save(Badge.builder()
                .name("Perfect Score").description("Score 100% on any quiz")
                .iconEmoji("💯").category("QUIZ").requiredCount(1).displayOrder(6).build());

        // Special badges
        badgeRepository.save(Badge.builder()
                .name("Welcome").description("Join the JavaMaster community")
                .iconEmoji("👋").category("SPECIAL").requiredCount(1).displayOrder(0).build());

        log.info("Seeded 7 initial badges");
    }
}
