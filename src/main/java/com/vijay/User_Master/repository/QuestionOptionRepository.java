package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {

    // Find all options for a question
    List<QuestionOption> findByQuestionIdOrderByDisplayOrderAsc(Long questionId);

    // Find correct option for a question
    QuestionOption findByQuestionIdAndIsCorrectTrue(Long questionId);
}
