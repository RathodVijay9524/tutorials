package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Find all questions for a quiz
    List<Question> findByQuizIdOrderByDisplayOrderAsc(Long quizId);

    // Find question with options
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.options WHERE q.id = :questionId")
    Optional<Question> findByIdWithOptions(@Param("questionId") Long questionId);

    // Count questions in a quiz
    long countByQuizId(Long quizId);
}
