package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.QuizAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    // Find all attempts by user
    List<QuizAttempt> findByUserIdOrderByStartedAtDesc(Long userId);

    // Find attempts by user and quiz
    List<QuizAttempt> findByUserIdAndQuizIdOrderByStartedAtDesc(Long userId, Long quizId);

    // Find paginated attempts by user
    Page<QuizAttempt> findByUserIdOrderByStartedAtDesc(Long userId, Pageable pageable);

    // Find incomplete attempt
    Optional<QuizAttempt> findByUserIdAndQuizIdAndIsCompletedFalse(Long userId, Long quizId);

    // Find best attempt (highest score)
    @Query("SELECT a FROM QuizAttempt a WHERE a.user.id = :userId AND a.quiz.id = :quizId AND a.isCompleted = true ORDER BY a.percentage DESC")
    List<QuizAttempt> findBestAttempts(@Param("userId") Long userId, @Param("quizId") Long quizId, Pageable pageable);

    // Count completed attempts
    long countByUserIdAndQuizIdAndIsCompletedTrue(Long userId, Long quizId);

    // Count passed attempts
    long countByUserIdAndQuizIdAndIsPassedTrue(Long userId, Long quizId);

    // Find attempt with responses
    @Query("SELECT a FROM QuizAttempt a LEFT JOIN FETCH a.responses r LEFT JOIN FETCH r.question LEFT JOIN FETCH r.selectedOption WHERE a.id = :attemptId")
    Optional<QuizAttempt> findByIdWithResponses(@Param("attemptId") Long attemptId);
}
