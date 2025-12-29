package com.vijay.User_Master.service;

import com.vijay.User_Master.dto.tutorial.*;
import com.vijay.User_Master.entity.*;
import com.vijay.User_Master.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningPathService {

    private final LearningPathRepository learningPathRepository;
    private final LearningPathStepRepository stepRepository;
    private final UserLearningPathRepository userLearningPathRepository;
    private final TutorialRepository tutorialRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;
    private final LearningPathRatingRepository ratingRepository;
    private final ModelMapper modelMapper;

    // ============= CRUD Operations =============

    @Transactional
    public LearningPathDTO createLearningPath(LearningPathRequest request) {
        User currentUser = getCurrentUser();
        
        LearningPath learningPath = LearningPath.builder()
                .name(request.getName())
                .description(request.getDescription())
                .goal(request.getGoal())
                .createdBy(currentUser)
                .isPublic(request.isPublic())
                .isFeatured(request.isFeatured())
                .difficultyLevel(request.getDifficultyLevel())
                .isAiGenerated(false)
                .build();

        learningPath = learningPathRepository.save(learningPath);

        // Add tutorials as steps
        int order = 1;
        for (Long tutorialId : request.getTutorialIds()) {
            Tutorial tutorial = tutorialRepository.findById(tutorialId)
                    .orElseThrow(() -> new RuntimeException("Tutorial not found: " + tutorialId));

            LearningPathStep step = LearningPathStep.builder()
                    .learningPath(learningPath)
                    .tutorial(tutorial)
                    .stepOrder(order++)
                    .build();

            learningPath.getSteps().add(step);
        }

        learningPath.calculateEstimatedHours();
        learningPath = learningPathRepository.save(learningPath);

        log.info("Created learning path: {} by user: {}", learningPath.getName(), currentUser.getUsername());
        return convertToDTO(learningPath, null);
    }

    @Transactional(readOnly = true)
    public LearningPathDTO getLearningPathById(Long id) {
        LearningPath learningPath = learningPathRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning path not found: " + id));

        UserLearningPath userProgress = null;
        try {
            User currentUser = getCurrentUser();
            userProgress = userLearningPathRepository
                    .findByUserIdAndLearningPathId(currentUser.getId(), id)
                    .orElse(null);
        } catch (Exception e) {
            // User not authenticated or not enrolled
        }

        return convertToDTO(learningPath, userProgress);
    }

    @Transactional(readOnly = true)
    public List<LearningPathDTO> getAllPublicLearningPaths() {
        List<LearningPath> paths = learningPathRepository
                .findByIsActiveTrueAndIsPublicTrueOrderByCreatedAtDesc();
        return paths.stream()
                .map(path -> convertToDTO(path, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<LearningPathDTO> searchAndFilterLearningPaths(
            String keyword,
            String difficulty,
            String sortBy,
            String sortDir,
            int page,
            int size) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, 
                           sortBy != null ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<LearningPath> paths;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Search by keyword
            List<LearningPath> searchResults = learningPathRepository.searchByKeyword(keyword.trim());
            
            // Apply difficulty filter if specified
            if (difficulty != null && !difficulty.trim().isEmpty()) {
                searchResults = searchResults.stream()
                        .filter(p -> difficulty.equalsIgnoreCase(p.getDifficultyLevel()))
                        .filter(p -> p.isActive() && p.isPublic())
                        .collect(Collectors.toList());
            } else {
                searchResults = searchResults.stream()
                        .filter(p -> p.isActive() && p.isPublic())
                        .collect(Collectors.toList());
            }
            
            // Apply sorting
            if (sortBy != null) {
                Comparator<LearningPath> comparator = getComparator(sortBy, sortDir);
                searchResults.sort(comparator);
            }
            
            // Manual pagination for search results
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), searchResults.size());
            List<LearningPath> paginatedResults = start < searchResults.size() 
                    ? searchResults.subList(start, end) 
                    : Collections.emptyList();
            
            paths = new org.springframework.data.domain.PageImpl<>(
                    paginatedResults, 
                    pageable, 
                    searchResults.size()
            );
        } else if (difficulty != null && !difficulty.trim().isEmpty()) {
            // Filter by difficulty only
            List<LearningPath> filtered = learningPathRepository
                    .findByDifficultyLevelAndIsActiveTrueAndIsPublicTrue(difficulty);
            
            if (sortBy != null) {
                Comparator<LearningPath> comparator = getComparator(sortBy, sortDir);
                filtered.sort(comparator);
            }
            
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filtered.size());
            List<LearningPath> paginatedResults = start < filtered.size() 
                    ? filtered.subList(start, end) 
                    : Collections.emptyList();
            
            paths = new org.springframework.data.domain.PageImpl<>(
                    paginatedResults, 
                    pageable, 
                    filtered.size()
            );
        } else {
            // Get all public paths with pagination
            paths = learningPathRepository.findByIsActiveTrueAndIsPublicTrue(pageable);
        }
        
        return paths.map(path -> convertToDTO(path, null));
    }
    
    private Comparator<LearningPath> getComparator(String sortBy, String sortDir) {
        Comparator<LearningPath> comparator;
        boolean ascending = sortDir.equalsIgnoreCase("asc");
        
        switch (sortBy.toLowerCase()) {
            case "name":
                comparator = Comparator.comparing(LearningPath::getName, 
                        ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());
                break;
            case "createdat":
            case "created_at":
                comparator = Comparator.comparing(LearningPath::getCreatedAt,
                        ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());
                break;
            case "enrollmentcount":
            case "enrollment_count":
                comparator = Comparator.comparing(LearningPath::getEnrollmentCount,
                        ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());
                break;
            case "rating":
            case "averagerating":
            case "average_rating":
                comparator = Comparator.comparing(LearningPath::getAverageRating,
                        ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());
                break;
            default:
                comparator = Comparator.comparing(LearningPath::getCreatedAt,
                        ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());
        }
        
        return comparator;
    }

    @Transactional(readOnly = true)
    public List<LearningPathDTO> getFeaturedLearningPaths() {
        List<LearningPath> paths = learningPathRepository
                .findByIsFeaturedTrueAndIsActiveTrueOrderByCreatedAtDesc();
        return paths.stream()
                .map(path -> convertToDTO(path, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public UserLearningPathDTO enrollInLearningPath(Long pathId) {
        User currentUser = getCurrentUser();
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Learning path not found: " + pathId));

        // Check if already enrolled
        Optional<UserLearningPath> existing = userLearningPathRepository
                .findByUserIdAndLearningPathId(currentUser.getId(), pathId);

        if (existing.isPresent()) {
            return convertUserLearningPathToDTO(existing.get());
        }

        // Create enrollment
        int totalSteps = (int) stepRepository.countByLearningPathId(pathId);
        UserLearningPath userLearningPath = UserLearningPath.builder()
                .user(currentUser)
                .learningPath(learningPath)
                .totalSteps(totalSteps)
                .completedSteps(0)
                .progressPercentage(0)
                .isCompleted(false)
                .build();

        // Calculate estimated completion date (assume 1 hour per day)
        if (learningPath.getEstimatedHours() != null) {
            userLearningPath.setEstimatedCompletionDate(
                    LocalDateTime.now().plusDays(learningPath.getEstimatedHours()));
        }

        userLearningPath = userLearningPathRepository.save(userLearningPath);

        // Update enrollment count
        learningPath.setEnrollmentCount(learningPath.getEnrollmentCount() + 1);
        learningPathRepository.save(learningPath);

        log.info("User {} enrolled in learning path: {}", currentUser.getUsername(), learningPath.getName());
        return convertUserLearningPathToDTO(userLearningPath);
    }

    @Transactional
    public void unenrollFromLearningPath(Long pathId) {
        User currentUser = getCurrentUser();
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Learning path not found: " + pathId));

        UserLearningPath enrollment = userLearningPathRepository
                .findByUserIdAndLearningPathId(currentUser.getId(), pathId)
                .orElseThrow(() -> new RuntimeException("You are not enrolled in this learning path"));

        // Delete enrollment
        userLearningPathRepository.delete(enrollment);

        // Update enrollment count
        learningPath.setEnrollmentCount(Math.max(0, learningPath.getEnrollmentCount() - 1));
        learningPathRepository.save(learningPath);

        log.info("User {} unenrolled from learning path: {}", currentUser.getUsername(), learningPath.getName());
    }

    // ============= Rating Operations =============

    @Transactional
    public LearningPathRatingDTO rateLearningPath(Long pathId, RatingRequest request) {
        User currentUser = getCurrentUser();
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Learning path not found: " + pathId));

        // Check if user already rated
        Optional<LearningPathRating> existingRating = ratingRepository
                .findByUserIdAndLearningPathId(currentUser.getId(), pathId);

        LearningPathRating rating;
        if (existingRating.isPresent()) {
            // Update existing rating
            rating = existingRating.get();
            rating.setRating(request.getRating());
            rating.setReview(request.getReview());
        } else {
            // Create new rating
            rating = LearningPathRating.builder()
                    .user(currentUser)
                    .learningPath(learningPath)
                    .rating(request.getRating())
                    .review(request.getReview())
                    .build();
        }

        rating = ratingRepository.save(rating);

        // Update learning path average rating
        updateLearningPathRatingStats(pathId);

        log.info("User {} rated learning path {} with {} stars", 
                currentUser.getUsername(), learningPath.getName(), request.getRating());
        
        return convertRatingToDTO(rating);
    }

    @Transactional(readOnly = true)
    public List<LearningPathRatingDTO> getLearningPathRatings(Long pathId) {
        List<LearningPathRating> ratings = ratingRepository.findByLearningPathIdOrderByCreatedAtDesc(pathId);
        return ratings.stream()
                .map(this::convertRatingToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LearningPathRatingDTO getUserRating(Long pathId) {
        User currentUser = getCurrentUser();
        Optional<LearningPathRating> rating = ratingRepository
                .findByUserIdAndLearningPathId(currentUser.getId(), pathId);
        
        return rating.map(this::convertRatingToDTO).orElse(null);
    }

    @Transactional
    public void deleteRating(Long pathId) {
        User currentUser = getCurrentUser();
        LearningPathRating rating = ratingRepository
                .findByUserIdAndLearningPathId(currentUser.getId(), pathId)
                .orElseThrow(() -> new RuntimeException("Rating not found"));

        ratingRepository.delete(rating);

        // Update learning path average rating
        updateLearningPathRatingStats(pathId);

        log.info("User {} deleted rating for learning path {}", 
                currentUser.getUsername(), pathId);
    }

    private void updateLearningPathRatingStats(Long pathId) {
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Learning path not found: " + pathId));

        Double averageRating = ratingRepository.calculateAverageRating(pathId);
        Long ratingCount = ratingRepository.countByLearningPathId(pathId);

        learningPath.setAverageRating(averageRating != null ? averageRating : 0.0);
        learningPath.setRatingCount(ratingCount != null ? ratingCount.intValue() : 0);
        learningPathRepository.save(learningPath);
    }

    private LearningPathRatingDTO convertRatingToDTO(LearningPathRating rating) {
        return LearningPathRatingDTO.builder()
                .id(rating.getId())
                .userId(rating.getUser().getId())
                .username(rating.getUser().getUsername())
                .learningPathId(rating.getLearningPath().getId())
                .rating(rating.getRating())
                .review(rating.getReview())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserLearningPathDTO> getUserLearningPaths() {
        User currentUser = getCurrentUser();
        List<UserLearningPath> userPaths = userLearningPathRepository
                .findByUserIdOrderByLastAccessedAtDesc(currentUser.getId());
        return userPaths.stream()
                .map(this::convertUserLearningPathToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateProgress(Long pathId, Long tutorialId) {
        User currentUser = getCurrentUser();
        UserLearningPath userPath = userLearningPathRepository
                .findByUserIdAndLearningPathId(currentUser.getId(), pathId)
                .orElseThrow(() -> new RuntimeException("Not enrolled in this learning path"));

        // Check if tutorial is in the path and get its order
        LearningPathStep step = stepRepository
                .findByLearningPathIdAndTutorialId(pathId, tutorialId)
                .orElse(null);

        if (step == null) {
            throw new RuntimeException("Tutorial is not part of this learning path");
        }

        // Check if user completed this tutorial
        Optional<UserProgress> progress = userProgressRepository
                .findByUserIdAndTutorialId(currentUser.getId(), tutorialId);

        if (progress.isPresent() && progress.get().isCompleted()) {
            // Count completed steps up to this point
            int completedSteps = (int) stepRepository.findByLearningPathIdOrderByStepOrderAsc(pathId)
                    .stream()
                    .filter(s -> s.getStepOrder() <= step.getStepOrder())
                    .filter(s -> {
                        Optional<UserProgress> p = userProgressRepository
                                .findByUserIdAndTutorialId(currentUser.getId(), s.getTutorial().getId());
                        return p.isPresent() && p.get().isCompleted();
                    })
                    .count();

            userPath.setCompletedSteps(completedSteps);
            userPath.calculateProgress();
            userLearningPathRepository.save(userPath);

            // Update completion count if path is completed
            if (userPath.isCompleted()) {
                LearningPath learningPath = learningPathRepository.findById(pathId).orElse(null);
                if (learningPath != null) {
                    learningPath.setCompletionCount(learningPath.getCompletionCount() + 1);
                    learningPathRepository.save(learningPath);
                }
            }
        }
    }

    // ============= Recommendation Engine =============

    @Transactional
    public RecommendationResponse generatePersonalizedLearningPath(RecommendationRequest request) {
        User currentUser = getCurrentUser();
        log.info("Generating personalized learning path for user: {} with goal: {}", 
                currentUser.getUsername(), request.getGoal());

        // Analyze user's current knowledge
        UserKnowledgeAnalysis analysis = analyzeUserKnowledge(currentUser.getId());

        // Find suitable tutorials
        List<Tutorial> candidateTutorials = findCandidateTutorials(request, analysis);

        // Order tutorials logically (prerequisites first, difficulty progression)
        List<Tutorial> orderedTutorials = orderTutorialsIntelligently(candidateTutorials, analysis);

        // Limit to requested number
        int maxTutorials = request.getMaxTutorials() != null ? request.getMaxTutorials() : 10;
        if (orderedTutorials.size() > maxTutorials) {
            orderedTutorials = orderedTutorials.subList(0, maxTutorials);
        }

        // Create learning path
        LearningPath learningPath = LearningPath.builder()
                .name(generatePathName(request.getGoal(), currentUser.getUsername()))
                .description(generatePathDescription(request.getGoal(), orderedTutorials.size()))
                .goal(request.getGoal())
                .createdBy(currentUser)
                .isPublic(false) // Personal paths are private by default
                .difficultyLevel(determineDifficultyLevel(orderedTutorials))
                .isAiGenerated(true)
                .build();

        learningPath = learningPathRepository.save(learningPath);

        // Add tutorials as steps
        int order = 1;
        for (Tutorial tutorial : orderedTutorials) {
            LearningPathStep step = LearningPathStep.builder()
                    .learningPath(learningPath)
                    .tutorial(tutorial)
                    .stepOrder(order++)
                    .build();
            learningPath.getSteps().add(step);
        }

        learningPath.calculateEstimatedHours();
        learningPath = learningPathRepository.save(learningPath);

        // Calculate confidence score
        double confidenceScore = calculateConfidenceScore(analysis, orderedTutorials, request);

        // Build recommendation response
        RecommendationResponse response = RecommendationResponse.builder()
                .recommendedPath(convertToDTO(learningPath, null))
                .reasoning(generateReasoning(analysis, orderedTutorials))
                .keyConcepts(extractKeyConcepts(orderedTutorials))
                .estimatedHours(learningPath.getEstimatedHours())
                .difficultyLevel(learningPath.getDifficultyLevel())
                .confidenceScore(confidenceScore)
                .build();

        log.info("Generated learning path with {} tutorials, confidence: {}", 
                orderedTutorials.size(), confidenceScore);

        return response;
    }

    @Transactional(readOnly = true)
    public List<LearningPathDTO> getRecommendedLearningPaths() {
        User currentUser = getCurrentUser();
        UserKnowledgeAnalysis analysis = analyzeUserKnowledge(currentUser.getId());

        // Get popular paths that match user's level
        List<LearningPath> popularPaths = learningPathRepository
                .findPopularLearningPaths(PageRequest.of(0, 10))
                .getContent();

        // Filter and score paths based on user's knowledge
        return popularPaths.stream()
                .filter(path -> isPathSuitable(path, analysis))
                .sorted((p1, p2) -> Double.compare(
                        scorePathRelevance(p2, analysis),
                        scorePathRelevance(p1, analysis)))
                .limit(5)
                .map(path -> convertToDTO(path, null))
                .collect(Collectors.toList());
    }

    // ============= Helper Methods =============

    private UserKnowledgeAnalysis analyzeUserKnowledge(Long userId) {
        List<UserProgress> completedProgress = userProgressRepository
                .findByUserIdAndIsCompletedTrue(userId);

        Set<Long> completedTutorialIds = completedProgress.stream()
                .map(p -> p.getTutorial().getId())
                .collect(Collectors.toSet());

        Set<Long> completedCategoryIds = completedProgress.stream()
                .map(p -> p.getTutorial().getCategory().getId())
                .collect(Collectors.toSet());

        // Determine user's difficulty level based on completed tutorials
        String userDifficultyLevel = determineUserDifficultyLevel(completedProgress);

        return UserKnowledgeAnalysis.builder()
                .completedTutorialIds(completedTutorialIds)
                .completedCategoryIds(completedCategoryIds)
                .completedCount(completedTutorialIds.size())
                .userDifficultyLevel(userDifficultyLevel)
                .build();
    }

    private List<Tutorial> findCandidateTutorials(RecommendationRequest request, 
                                                   UserKnowledgeAnalysis analysis) {
        // Start with all published tutorials
        List<Tutorial> allTutorials = tutorialRepository.findByIsPublishedTrue(
                Pageable.unpaged()).getContent();

        return allTutorials.stream()
                .filter(tutorial -> {
                    // Exclude already completed tutorials
                    if (analysis.getCompletedTutorialIds().contains(tutorial.getId())) {
                        return false;
                    }

                    // Exclude explicitly excluded tutorials
                    if (request.getExcludeTutorialIds() != null &&
                        request.getExcludeTutorialIds().contains(tutorial.getId())) {
                        return false;
                    }

                    // Filter by difficulty if specified
                    if (request.getDifficultyLevel() != null &&
                        !request.getDifficultyLevel().equals(tutorial.getDifficulty())) {
                        return false;
                    }

                    // Filter by preferred categories if specified
                    if (request.getPreferredCategoryIds() != null &&
                        !request.getPreferredCategoryIds().isEmpty() &&
                        !request.getPreferredCategoryIds().contains(tutorial.getCategory().getId())) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    private List<Tutorial> orderTutorialsIntelligently(List<Tutorial> tutorials, 
                                                       UserKnowledgeAnalysis analysis) {
        return tutorials.stream()
                .sorted((t1, t2) -> {
                    // 1. Order by difficulty (BEGINNER first)
                    int difficultyCompare = compareDifficulty(t1.getDifficulty(), t2.getDifficulty());
                    if (difficultyCompare != 0) return difficultyCompare;

                    // 2. Order by category (group related tutorials)
                    int categoryCompare = t1.getCategory().getId().compareTo(t2.getCategory().getId());
                    if (categoryCompare != 0) return categoryCompare;

                    // 3. Order by popularity (view count)
                    return Long.compare(t2.getViewCount(), t1.getViewCount());
                })
                .collect(Collectors.toList());
    }

    private int compareDifficulty(String d1, String d2) {
        Map<String, Integer> difficultyOrder = Map.of(
                "BEGINNER", 1,
                "INTERMEDIATE", 2,
                "ADVANCED", 3
        );
        return Integer.compare(
                difficultyOrder.getOrDefault(d1, 2),
                difficultyOrder.getOrDefault(d2, 2)
        );
    }

    private String determineDifficultyLevel(List<Tutorial> tutorials) {
        if (tutorials.isEmpty()) return "BEGINNER";

        long beginnerCount = tutorials.stream()
                .filter(t -> "BEGINNER".equals(t.getDifficulty()))
                .count();
        long advancedCount = tutorials.stream()
                .filter(t -> "ADVANCED".equals(t.getDifficulty()))
                .count();

        if (advancedCount > tutorials.size() / 2) return "ADVANCED";
        if (beginnerCount > tutorials.size() / 2) return "BEGINNER";
        return "INTERMEDIATE";
    }

    private String determineUserDifficultyLevel(List<UserProgress> completedProgress) {
        if (completedProgress.isEmpty()) return "BEGINNER";

        long advancedCount = completedProgress.stream()
                .filter(p -> "ADVANCED".equals(p.getTutorial().getDifficulty()))
                .count();

        if (advancedCount > completedProgress.size() / 3) return "ADVANCED";
        if (completedProgress.size() > 10) return "INTERMEDIATE";
        return "BEGINNER";
    }

    private double calculateConfidenceScore(UserKnowledgeAnalysis analysis, 
                                            List<Tutorial> tutorials, 
                                            RecommendationRequest request) {
        double score = 0.5; // Base score

        // Increase score if tutorials match user's level
        long matchingDifficulty = tutorials.stream()
                .filter(t -> t.getDifficulty().equals(analysis.getUserDifficultyLevel()))
                .count();
        score += (matchingDifficulty / (double) tutorials.size()) * 0.3;

        // Increase score if goal is specific
        if (request.getGoal() != null && request.getGoal().length() > 10) {
            score += 0.1;
        }

        // Increase score if we have enough tutorials
        if (tutorials.size() >= 5) {
            score += 0.1;
        }

        return Math.min(1.0, score);
    }

    private String generatePathName(String goal, String username) {
        if (goal != null && !goal.isEmpty()) {
            return "Path: " + goal;
        }
        return "Personalized Learning Path for " + username;
    }

    private String generatePathDescription(String goal, int tutorialCount) {
        return String.format("A personalized learning path with %d tutorials. Goal: %s", 
                tutorialCount, goal != null ? goal : "Master Java Programming");
    }

    private String generateReasoning(UserKnowledgeAnalysis analysis, List<Tutorial> tutorials) {
        StringBuilder reasoning = new StringBuilder();
        reasoning.append("Based on your progress (").append(analysis.getCompletedCount())
                .append(" tutorials completed), ");
        reasoning.append("we've selected ").append(tutorials.size())
                .append(" tutorials that match your current skill level (")
                .append(analysis.getUserDifficultyLevel()).append("). ");
        reasoning.append("The path progresses from foundational concepts to more advanced topics.");
        return reasoning.toString();
    }

    private List<String> extractKeyConcepts(List<Tutorial> tutorials) {
        return tutorials.stream()
                .map(Tutorial::getCategory)
                .map(TutorialCategory::getName)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }

    private boolean isPathSuitable(LearningPath path, UserKnowledgeAnalysis analysis) {
        // Check if path difficulty matches user level
        if (path.getDifficultyLevel() != null) {
            int pathLevel = getDifficultyLevelValue(path.getDifficultyLevel());
            int userLevel = getDifficultyLevelValue(analysis.getUserDifficultyLevel());
            
            // Allow paths at user's level or one level above
            return pathLevel <= userLevel + 1;
        }
        return true;
    }

    private double scorePathRelevance(LearningPath path, UserKnowledgeAnalysis analysis) {
        double score = 0.0;

        // Base score from popularity
        score += path.getEnrollmentCount() * 0.1;
        score += path.getAverageRating() * 20;

        // Bonus for matching difficulty
        if (path.getDifficultyLevel() != null &&
            path.getDifficultyLevel().equals(analysis.getUserDifficultyLevel())) {
            score += 50;
        }

        return score;
    }

    private int getDifficultyLevelValue(String level) {
        return switch (level != null ? level.toUpperCase() : "BEGINNER") {
            case "BEGINNER" -> 1;
            case "INTERMEDIATE" -> 2;
            case "ADVANCED" -> 3;
            default -> 1;
        };
    }

    private LearningPathDTO convertToDTO(LearningPath path, UserLearningPath userProgress) {
        LearningPathDTO dto = modelMapper.map(path, LearningPathDTO.class);
        
        if (path.getCreatedBy() != null) {
            dto.setCreatedById(path.getCreatedBy().getId());
            dto.setCreatedByName(path.getCreatedBy().getName());
        }

        // Convert steps
        if (path.getSteps() != null) {
            dto.setSteps(path.getSteps().stream()
                    .map(step -> convertStepToDTO(step, userProgress))
                    .collect(Collectors.toList()));
        }

        // Add user progress if available
        if (userProgress != null) {
            dto.setUserProgress(convertUserLearningPathToDTO(userProgress));
        }

        return dto;
    }

    private LearningPathStepDTO convertStepToDTO(LearningPathStep step, UserLearningPath userProgress) {
        LearningPathStepDTO dto = modelMapper.map(step, LearningPathStepDTO.class);
        
        if (step.getTutorial() != null) {
            Tutorial tutorial = step.getTutorial();
            dto.setTutorialId(tutorial.getId());
            dto.setTutorialTitle(tutorial.getTitle());
            dto.setTutorialSlug(tutorial.getSlug());
            dto.setTutorialDifficulty(tutorial.getDifficulty());
            dto.setTutorialEstimatedMinutes(tutorial.getEstimatedMinutes());

            // Check if user completed this step
            if (userProgress != null) {
                Optional<UserProgress> progress = userProgressRepository
                        .findByUserIdAndTutorialId(userProgress.getUser().getId(), tutorial.getId());
                if (progress.isPresent()) {
                    dto.setCompleted(progress.get().isCompleted());
                    dto.setProgressPercentage(progress.get().getProgressPercentage());
                }
            }
        }

        return dto;
    }

    private UserLearningPathDTO convertUserLearningPathToDTO(UserLearningPath userPath) {
        UserLearningPathDTO dto = modelMapper.map(userPath, UserLearningPathDTO.class);
        
        if (userPath.getLearningPath() != null) {
            dto.setLearningPathName(userPath.getLearningPath().getName());
        }

        return dto;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }
        return user;
    }

    // Inner class for knowledge analysis
    @lombok.Data
    @lombok.Builder
    private static class UserKnowledgeAnalysis {
        private Set<Long> completedTutorialIds;
        private Set<Long> completedCategoryIds;
        private Integer completedCount;
        private String userDifficultyLevel;
    }
}

