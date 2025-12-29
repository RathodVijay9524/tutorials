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

import java.time.LocalDateTime;
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
        List<Quiz> quizzes = quizRepository.findByTutorialIdWithQuestions(tutorialId);
        
        if (quizzes.isEmpty()) {
            return null;
        }
        
        // Pick the most recent one (sorted by ID DESC in repository)
        Quiz quiz = quizzes.get(0);
        
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
        
        // Create new attempt with time limit
        Integer timeLimitSeconds = null;
        if (quiz.getTimeLimitMinutes() != null && quiz.getTimeLimitMinutes() > 0) {
            timeLimitSeconds = quiz.getTimeLimitMinutes() * 60;
        }
        
        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .timeLimitSeconds(timeLimitSeconds)
                .timeRemainingSeconds(timeLimitSeconds)
                .timeSpentSeconds(0)
                .build();
        
        attempt = attemptRepository.save(attempt);
        log.info("User {} started quiz attempt {} for quiz {} with time limit {} seconds", 
                user.getUsername(), attempt.getId(), quizId, timeLimitSeconds);
        
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
                    .correct(isCorrect)
                    .pointsEarned(pointsEarned)
                    .build();
            
            responses.add(response);
        }
        
        // Use the managed collection of the attempt
        attempt.getResponses().clear();
        attempt.getResponses().addAll(responses);
        
        // Calculate time spent
        if (attempt.getStartedAt() != null) {
            long secondsSpent = java.time.Duration.between(attempt.getStartedAt(), LocalDateTime.now()).getSeconds();
            attempt.setTimeSpentSeconds((int) secondsSpent);
            
            // Update time remaining if time limit exists
            if (attempt.getTimeLimitSeconds() != null) {
                int remaining = attempt.getTimeLimitSeconds() - attempt.getTimeSpentSeconds();
                attempt.setTimeRemainingSeconds(Math.max(0, remaining));
            }
        }
        
        // Complete the attempt
        attempt.completeAttempt(earnedScore, totalScore, quiz.getPassingScore());
        attempt = attemptRepository.save(attempt);
        
        log.info("User {} completed quiz {} with score {}/{} ({}%) in {} seconds", 
                user.getUsername(), quiz.getTitle(), earnedScore, totalScore, attempt.getPercentage(), attempt.getTimeSpentSeconds());
        
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

    /**
     * Create or update a quiz
     */
    @Transactional
    public QuizDTO saveQuiz(QuizDTO quizDTO) {
        Quiz quiz;
        if (quizDTO.getId() != null) {
            quiz = quizRepository.findById(quizDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Quiz not found"));
        } else {
            quiz = new Quiz();
            Tutorial tutorial = tutorialRepository.findById(quizDTO.getTutorialId())
                    .orElseThrow(() -> new RuntimeException("Tutorial not found"));
            quiz.setTutorial(tutorial);
        }

        quiz.setTitle(quizDTO.getTitle());
        quiz.setDescription(quizDTO.getDescription());
        quiz.setPassingScore(quizDTO.getPassingScore());
        quiz.setTimeLimitMinutes(quizDTO.getTimeLimitMinutes());
        quiz.setActive(quizDTO.isActive());

        // Handle Questions if provided
        if (quizDTO.getQuestions() != null) {
            // For simplicity in this implementation, we might want to handle questions separately
            // but let's at least save the basic quiz info first.
        }

        quiz = quizRepository.save(quiz);
        return convertToDTO(quiz, true);
    }

    /**
     * Delete a quiz
     */
    @Transactional
    public void deleteQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        quizRepository.delete(quiz);
    }

    /**
     * Get all quizzes for admin listing
     */
    @Transactional(readOnly = true)
    public List<QuizDTO> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(q -> convertToDTO(q, true))
                .collect(Collectors.toList());
    }

    /**
     * Get a question by ID
     */
    @Transactional(readOnly = true)
    public QuestionDTO getQuestionById(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        return convertQuestionToDTO(question, true);
    }

    /**
     * Add a question to a quiz
     */
    @Transactional
    public QuestionDTO addQuestion(Long quizId, QuestionDTO questionDTO) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        Question question = Question.builder()
                .quiz(quiz)
                .questionText(questionDTO.getQuestionText())
                .questionType(questionDTO.getQuestionType() != null ? questionDTO.getQuestionType() : "MULTIPLE_CHOICE")
                .codeSnippet(questionDTO.getCodeSnippet())
                .explanation(questionDTO.getExplanation())
                .displayOrder(questionDTO.getDisplayOrder() != null ? questionDTO.getDisplayOrder() : 0)
                .points(questionDTO.getPoints() != null ? questionDTO.getPoints() : 1)
                .build();

        question = questionRepository.save(question);

        // Save initial options if provided
        if (questionDTO.getOptions() != null) {
            for (QuestionOptionDTO optionDTO : questionDTO.getOptions()) {
                if (optionDTO.getOptionText() != null && !optionDTO.getOptionText().isBlank()) {
                    QuestionOption option = QuestionOption.builder()
                            .question(question)
                            .optionText(optionDTO.getOptionText())
                            .correct(optionDTO.isCorrect())
                            .displayOrder(optionDTO.getDisplayOrder() != null ? optionDTO.getDisplayOrder() : 0)
                            .build();
                    optionRepository.save(option);
                }
            }
        }

        return convertQuestionToDTO(question, true);
    }

    /**
     * Update a question
     */
    @Transactional
    public QuestionDTO updateQuestion(Long questionId, QuestionDTO questionDTO) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        question.setQuestionText(questionDTO.getQuestionText());
        question.setQuestionType(questionDTO.getQuestionType());
        question.setCodeSnippet(questionDTO.getCodeSnippet());
        question.setExplanation(questionDTO.getExplanation());
        question.setDisplayOrder(questionDTO.getDisplayOrder());
        question.setPoints(questionDTO.getPoints() != null ? questionDTO.getPoints() : 1);

        // Update options
        if (questionDTO.getOptions() != null) {
            // Simplest way is to remove old and add new if we are sending the full list
            // But usually, we might want to update individually.
            // For now, let's keep it simple for the admin UI.
        }

        question = questionRepository.save(question);
        return convertQuestionToDTO(question, true);
    }

    /**
     * Delete a question
     */
    @Transactional
    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        questionRepository.delete(question);
    }

    /**
     * Add or update an option
     */
    @Transactional
    public QuestionOptionDTO saveOption(Long questionId, QuestionOptionDTO optionDTO) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        QuestionOption option;
        if (optionDTO.getId() != null) {
            option = optionRepository.findById(optionDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Option not found"));
        } else {
            option = new QuestionOption();
            option.setQuestion(question);
        }

        option.setOptionText(optionDTO.getOptionText());
        option.setCorrect(optionDTO.isCorrect());
        option.setDisplayOrder(optionDTO.getDisplayOrder());

        option = optionRepository.save(option);
        return QuestionOptionDTO.builder()
                .id(option.getId())
                .optionText(option.getOptionText())
                .displayOrder(option.getDisplayOrder())
                .correct(option.isCorrect())
                .build();
    }

    /**
     * Delete an option
     */
    @Transactional
    public void deleteOption(Long optionId) {
        QuestionOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new RuntimeException("Option not found"));
        optionRepository.delete(option);
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
                        .correct(includeCorrectAnswers ? o.isCorrect() : false)
                        .build())
                .collect(Collectors.toList());
        
        return QuestionDTO.builder()
                .id(question.getId())
                .quizId(question.getQuiz().getId())
                .quizTitle(question.getQuiz().getTitle())
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
                .timeLimitSeconds(attempt.getTimeLimitSeconds())
                .timeRemainingSeconds(attempt.getTimeRemainingSeconds())
                .timeSpentSeconds(attempt.getTimeSpentSeconds())
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
                .correct(response.isCorrect())
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
            QuestionOption.builder().question(q1).optionText("var count = 10;").correct(false).displayOrder(1).build(),
            QuestionOption.builder().question(q1).optionText("int count = 10;").correct(true).displayOrder(2).build(),
            QuestionOption.builder().question(q1).optionText("decimal count = 10;").correct(false).displayOrder(3).build(),
            QuestionOption.builder().question(q1).optionText("count = 10;").correct(false).displayOrder(4).build()
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
            QuestionOption.builder().question(q2).optionText("create").correct(false).displayOrder(1).build(),
            QuestionOption.builder().question(q2).optionText("make").correct(false).displayOrder(2).build(),
            QuestionOption.builder().question(q2).optionText("new").correct(true).displayOrder(3).build(),
            QuestionOption.builder().question(q2).optionText("alloc").correct(false).displayOrder(4).build()
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
            QuestionOption.builder().question(q3).optionText("x=6, y=6").correct(false).displayOrder(1).build(),
            QuestionOption.builder().question(q3).optionText("x=5, y=5").correct(false).displayOrder(2).build(),
            QuestionOption.builder().question(q3).optionText("x=6, y=5").correct(true).displayOrder(3).build(),
            QuestionOption.builder().question(q3).optionText("x=5, y=6").correct(false).displayOrder(4).build()
        ));

        log.info("Sample quiz seeded successfully for tutorial: {}", tutorial.getTitle());
    }

    /**
     * Update time remaining for a quiz attempt
     */
    @Transactional
    public QuizAttemptDTO updateTimeRemaining(Long attemptId, Integer timeRemainingSeconds) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        
        User user = getCurrentUser();
        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot update another user's quiz attempt");
        }
        
        if (attempt.isCompleted()) {
            throw new IllegalStateException("Cannot update time for completed attempt");
        }
        
        attempt.setTimeRemainingSeconds(timeRemainingSeconds);
        
        // Calculate time spent
        if (attempt.getStartedAt() != null && attempt.getTimeLimitSeconds() != null) {
            int timeSpent = attempt.getTimeLimitSeconds() - timeRemainingSeconds;
            attempt.setTimeSpentSeconds(Math.max(0, timeSpent));
        }
        
        attempt = attemptRepository.save(attempt);
        return convertAttemptToDTO(attempt, false);
    }
}

