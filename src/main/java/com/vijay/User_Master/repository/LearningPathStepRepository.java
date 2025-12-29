package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.LearningPathStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningPathStepRepository extends JpaRepository<LearningPathStep, Long> {

    // Find all steps for a learning path, ordered
    List<LearningPathStep> findByLearningPathIdOrderByStepOrderAsc(Long learningPathId);

    // Find step by learning path and step order
    Optional<LearningPathStep> findByLearningPathIdAndStepOrder(Long learningPathId, Integer stepOrder);

    // Count steps in a learning path
    long countByLearningPathId(Long learningPathId);

    // Find next step for a user
    @Query("SELECT s FROM LearningPathStep s " +
           "WHERE s.learningPath.id = :pathId AND s.stepOrder > :currentStep " +
           "ORDER BY s.stepOrder ASC")
    List<LearningPathStep> findNextSteps(@Param("pathId") Long pathId, @Param("currentStep") Integer currentStep);

    // Check if tutorial is in any learning path
    boolean existsByTutorialId(Long tutorialId);

    // Find step by learning path and tutorial
    Optional<LearningPathStep> findByLearningPathIdAndTutorialId(Long learningPathId, Long tutorialId);
}

