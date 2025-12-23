package com.vijay.User_Master.service;

import com.vijay.User_Master.dto.tutorial.*;
import com.vijay.User_Master.entity.*;
import com.vijay.User_Master.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuizResponseRepository responseRepository;
    private final UserRepository userRepository;
    private final TutorialRepository tutorialRepository;

    /**
     * Get the active quiz for a tutorial (without correct answers)
     */
    @Transactional(readOnly = true)
    public QuizDTO getQuizForTutorial(Long tutorialId) {
        Quiz quiz = quizRepository.findByTutorialIdWithQuestions(tutorialId)
                .orElse(null);
        
        if (quiz == null) {
            return null;
        }
        
        return convertToDTO(quiz, false);
    }

    /**
     * Get quiz by ID
     */
    @Transactional(readOnly = true)
    public QuizDTO getQuizById(Long quizId) {
        Quiz quiz = quizRepository.findByIdWithQuestions(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));
        
        return convertToDTO(quiz, false);
    }

    /**
     * Start a new quiz attempt
     */
    @Transactional
    public QuizAttemptDTO startQuizAttempt(Long quizId) {
        User user = getCurrentUser();
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        
        // Check for existing incomplete attempt
        QuizAttempt existingAttempt = attemptRepository
                .findByUserIdAndQuizIdAndIsCompletedFalse(user.getId(), quizId)
                .orElse(null);
        
        if (existingAttempt != null) {
            log.info("Returning existing incomplete attempt for user {} on quiz {}", user.getUsername(), quizId);
            return convertAttemptToDTO(existingAttempt, false);
        }
        
        // Create new attempt
        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .build();
        
        attempt = attemptRepository.save(attempt);
        log.info("User {} started quiz attempt {} for quiz {}", user.getUsername(), attempt.getId(), quizId);
        
        return convertAttemptToDTO(attempt, false);
    }

    /**
     * Submit quiz answers and calculate score
     */
    @Transactional
    public QuizAttemptDTO submitQuiz(SubmitQuizRequest request) {
        User user = getCurrentUser();
        
        QuizAttempt attempt = attemptRepository.findById(request.getAttemptId())
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        
        // Verify ownership
        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot submit another user's quiz attempt");
        }
        
        if (attempt.isCompleted()) {
            throw new IllegalStateException("Quiz attempt already completed");
        }
        
        Quiz quiz = quizRepository.findByIdWithQuestions(attempt.getQuiz().getId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        
        int totalScore = 0;
        int earnedScore = 0;
        List<QuizResponse> responses = new ArrayList<>();
        
        for (Question question : quiz.getQuestions()) {
            Long selectedOptionId = request.getAnswers() != null ? request.getAnswers().get(question.getId()) : null;
            QuestionOption correctOption = question.getCorrectOption();
            
            int questionPoints = question.getPoints() != null ? question.getPoints() : 1;
            totalScore += questionPoints;
            
            boolean isCorrect = false;
            int pointsEarned = 0;
            
            if (selectedOptionId != null && correctOption != null && 
                selectedOptionId.equals(correctOption.getId())) {
                isCorrect = true;
                pointsEarned = questionPoints;
                earnedScore += pointsEarned;
            }
            
            QuestionOption selectedOption = selectedOptionId != null ? 
                    optionRepository.findById(selectedOptionId).orElse(null) : null;
            
            QuizResponse response = QuizResponse.builder()
                    .quizAttempt(attempt)
                    .question(question)
                    .selectedOption(selectedOption)
                    .isCorrect(isCorrect)
                    .pointsEarned(pointsEarned)
                    .build();
            
            responses.add(response);
        }
        
        // Use the managed collection of the attempt
        attempt.getResponses().clear();
        attempt.getResponses().addAll(responses);
        
        // Complete the attempt
        attempt.completeAttempt(earnedScore, totalScore, quiz.getPassingScore());
        attempt = attemptRepository.save(attempt);
        
        log.info("User {} completed quiz {} with score {}/{} ({}%)", 
                user.getUsername(), quiz.getTitle(), earnedScore, totalScore, attempt.getPercentage());
        
        return convertAttemptToDTO(attempt, true);
    }

    /**
     * Get a specific attempt with detailed results
     */
    @Transactional(readOnly = true)
    public QuizAttemptDTO getAttemptResult(Long attemptId) {
        QuizAttempt attempt = attemptRepository.findByIdWithResponses(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        
        return convertAttemptToDTO(attempt, true);
    }

    /**
     * Get user's quiz history for a specific quiz
     */
    @Transactional(readOnly = true)
    public List<QuizAttemptDTO> getUserQuizHistory(Long quizId) {
        User user = getCurrentUser();
        List<QuizAttempt> attempts = attemptRepository.findByUserIdAndQuizIdOrderByStartedAtDesc(user.getId(), quizId);
        
        return attempts.stream()
                .map(a -> convertAttemptToDTO(a, false))
                .collect(Collectors.toList());
    }

    /**
     * Get user's best attempt for a quiz
     */
    @Transactional(readOnly = true)
    public QuizAttemptDTO getBestAttempt(Long quizId) {
        User user = getCurrentUser();
        List<QuizAttempt> best = attemptRepository.findBestAttempts(user.getId(), quizId, PageRequest.of(0, 1));
        
        if (best.isEmpty()) {
            return null;
        }
        
        return convertAttemptToDTO(best.get(0), false);
    }

    // ============ Helper Methods ============

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private QuizDTO convertToDTO(Quiz quiz, boolean includeCorrectAnswers) {
        List<QuestionDTO> questionDTOs = quiz.getQuestions().stream()
                .map(q -> convertQuestionToDTO(q, includeCorrectAnswers))
                .collect(Collectors.toList());
        
        return QuizDTO.builder()
                .id(quiz.getId())
                .tutorialId(quiz.getTutorial().getId())
                .tutorialTitle(quiz.getTutorial().getTitle())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .passingScore(quiz.getPassingScore())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .isActive(quiz.isActive())
                .questionCount(quiz.getQuestions().size())
                .totalPoints(quiz.getTotalPoints())
                .questions(questionDTOs)
                .createdAt(quiz.getCreatedAt())
                .build();
    }

    private QuestionDTO convertQuestionToDTO(Question question, boolean includeCorrectAnswers) {
        List<QuestionOptionDTO> optionDTOs = question.getOptions().stream()
                .sorted((a, b) -> {
                    int orderA = a.getDisplayOrder() != null ? a.getDisplayOrder() : 0;
                    int orderB = b.getDisplayOrder() != null ? b.getDisplayOrder() : 0;
                    return Integer.compare(orderA, orderB);
                })
                .map(o -> QuestionOptionDTO.builder()
                        .id(o.getId())
                        .optionText(o.getOptionText())
                        .displayOrder(o.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
        
        return QuestionDTO.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .codeSnippet(question.getCodeSnippet())
                .explanation(includeCorrectAnswers ? question.getExplanation() : null)
                .displayOrder(question.getDisplayOrder())
                .points(question.getPoints())
                .options(optionDTOs)
                .build();
    }

    private QuizAttemptDTO convertAttemptToDTO(QuizAttempt attempt, boolean includeResponses) {
        List<QuizResponseDTO> responseDTOs = null;
        
        if (includeResponses && attempt.getResponses() != null) {
            responseDTOs = attempt.getResponses().stream()
                    .map(this::convertResponseToDTO)
                    .collect(Collectors.toList());
        }
        
        return QuizAttemptDTO.builder()
                .id(attempt.getId())
                .userId(attempt.getUser().getId())
                .userName(attempt.getUser().getName())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .score(attempt.getScore())
                .maxScore(attempt.getMaxScore())
                .percentage(attempt.getPercentage())
                .isPassed(attempt.isPassed())
                .isCompleted(attempt.isCompleted())
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .responses(responseDTOs)
                .build();
    }

    private QuizResponseDTO convertResponseToDTO(QuizResponse response) {
        Question question = response.getQuestion();
        QuestionOption correctOption = question.getCorrectOption();
        
        return QuizResponseDTO.builder()
                .id(response.getId())
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .selectedOptionId(response.getSelectedOption() != null ? response.getSelectedOption().getId() : null)
                .selectedOptionText(response.getSelectedOption() != null ? response.getSelectedOption().getOptionText() : null)
                .correctOptionId(correctOption != null ? correctOption.getId() : null)
                .correctOptionText(correctOption != null ? correctOption.getOptionText() : null)
                .isCorrect(response.isCorrect())
                .pointsEarned(response.getPointsEarned())
                .explanation(question.getExplanation())
                .build();
    }

    /**
     * Programmatically seed sample quiz data for testing
     */
    @Transactional
    public void seedSampleQuiz() {
        // Find first tutorial
        Tutorial tutorial = tutorialRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No tutorials found to seed quiz"));

        // Delete existing attempts first to avoid foreign key constraints
        List<Quiz> existingQuizzes = quizRepository.findByTutorialId(tutorial.getId());
        for (Quiz q : existingQuizzes) {
            // Delete all attempts for this quiz manually to avoid FK issues
            List<QuizAttempt> allAttemptsForQuiz = attemptRepository.findAll().stream()
                    .filter(a -> a.getQuiz().getId().equals(q.getId()))
                    .collect(Collectors.toList());
            attemptRepository.deleteAll(allAttemptsForQuiz);
            quizRepository.delete(q);
        }

        Quiz quiz = Quiz.builder()
                .tutorial(tutorial)
                .title("Java Fundamentals Mastery")
                .description("Test your knowledge of core Java concepts like variables, syntax, and methods.")
                .passingScore(70)
                .timeLimitMinutes(15)
                .isActive(true)
                .build();

        quiz = quizRepository.save(quiz);

        // Q1
        Question q1 = Question.builder()
                .quiz(quiz)
                .questionText("Which of these is the correct way to declare an integer variable 'count' initialized to 10?")
                .questionType("MULTIPLE_CHOICE")
                .explanation("In Java, we use 'int' followed by the name and assignment.")
                .displayOrder(1)
                .points(1)
                .build();
        q1 = questionRepository.save(q1);
        
        optionRepository.saveAll(List.of(
            QuestionOption.builder().question(q1).optionText("var count = 10;").isCorrect(false).displayOrder(1).build(),
            QuestionOption.builder().question(q1).optionText("int count = 10;").isCorrect(true).displayOrder(2).build(),
            QuestionOption.builder().question(q1).optionText("decimal count = 10;").isCorrect(false).displayOrder(3).build(),
            QuestionOption.builder().question(q1).optionText("count = 10;").isCorrect(false).displayOrder(4).build()
        ));

        // Q2
        Question q2 = Question.builder()
                .quiz(quiz)
                .questionText("What keyword is used to create a new object from a class?")
                .questionType("MULTIPLE_CHOICE")
                .explanation("The 'new' operator is used to instantiate objects.")
                .displayOrder(2)
                .points(1)
                .build();
        q2 = questionRepository.save(q2);

        optionRepository.saveAll(List.of(
            QuestionOption.builder().question(q2).optionText("create").isCorrect(false).displayOrder(1).build(),
            QuestionOption.builder().question(q2).optionText("make").isCorrect(false).displayOrder(2).build(),
            QuestionOption.builder().question(q2).optionText("new").isCorrect(true).displayOrder(3).build(),
            QuestionOption.builder().question(q2).optionText("alloc").isCorrect(false).displayOrder(4).build()
        ));

        // Q3 (With Code)
        Question q3 = Question.builder()
                .quiz(quiz)
                .questionText("Look at the code. What will be the output?")
                .questionType("MULTIPLE_CHOICE")
                .codeSnippet("int x = 5;\nint y = x++;\nSystem.out.println(\"x=\" + x + \", y=\" + y);")
                .explanation("x++ is post-increment: y gets the value 5, then x becomes 6.")
                .displayOrder(3)
                .points(1)
                .build();
        q3 = questionRepository.save(q3);

        optionRepository.saveAll(List.of(
            QuestionOption.builder().question(q3).optionText("x=6, y=6").isCorrect(false).displayOrder(1).build(),
            QuestionOption.builder().question(q3).optionText("x=5, y=5").isCorrect(false).displayOrder(2).build(),
            QuestionOption.builder().question(q3).optionText("x=6, y=5").isCorrect(true).displayOrder(3).build(),
            QuestionOption.builder().question(q3).optionText("x=5, y=6").isCorrect(false).displayOrder(4).build()
        ));

        log.info("Sample quiz seeded successfully for tutorial: {}", tutorial.getTitle());
    }
}

