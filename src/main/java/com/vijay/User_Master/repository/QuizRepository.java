package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // Find all quizzes for a tutorial
    List<Quiz> findByTutorialId(Long tutorialId);

    // Find active quiz for a tutorial
    Optional<Quiz> findByTutorialIdAndIsActiveTrue(Long tutorialId);

    // Find quiz with questions eagerly loaded (single join to avoid MultipleBagFetchException)
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :quizId")
    Optional<Quiz> findByIdWithQuestions(@Param("quizId") Long quizId);

    // Find quiz by tutorial with questions (single join to avoid MultipleBagFetchException)
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.tutorial.id = :tutorialId AND q.isActive = true")
    Optional<Quiz> findByTutorialIdWithQuestions(@Param("tutorialId") Long tutorialId);

    // Count quizzes for a tutorial
    long countByTutorialId(Long tutorialId);
}
