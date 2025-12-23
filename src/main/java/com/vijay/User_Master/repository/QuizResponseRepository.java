package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.QuizResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizResponseRepository extends JpaRepository<QuizResponse, Long> {

    // Find all responses for an attempt
    List<QuizResponse> findByQuizAttemptId(Long attemptId);

    // Check if user answered a specific question in an attempt
    boolean existsByQuizAttemptIdAndQuestionId(Long attemptId, Long questionId);
}
