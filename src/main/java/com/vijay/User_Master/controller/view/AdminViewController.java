package com.vijay.User_Master.controller.view;

import com.vijay.User_Master.dto.tutorial.TutorialCategoryDTO;
import com.vijay.User_Master.dto.tutorial.TutorialDTO;
import com.vijay.User_Master.dto.tutorial.QuestionDTO;
import com.vijay.User_Master.dto.tutorial.QuestionOptionDTO;
import com.vijay.User_Master.dto.tutorial.QuizDTO;
import com.vijay.User_Master.service.QuizService;
import com.vijay.User_Master.service.TutorialCategoryService;
import com.vijay.User_Master.service.TutorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminViewController {

    private final TutorialCategoryService categoryService;
    private final TutorialService tutorialService;
    private final QuizService quizService;

    // ========== DASHBOARD ==========

    @GetMapping
    public String adminDashboard(Model model) {
        List<TutorialCategoryDTO> categories = categoryService.getAllCategories();
        Page<TutorialDTO> tutorials = tutorialService.getAllPublishedTutorials(0, 1000, "id", "asc");
        List<QuizDTO> quizzes = quizService.getAllQuizzes();
        
        model.addAttribute("categoryCount", categories.size());
        model.addAttribute("tutorialCount", tutorials.getTotalElements());
        model.addAttribute("quizCount", quizzes.size());
        model.addAttribute("title", "Admin Dashboard");
        return "admin/dashboard";
    }

    // ========== CATEGORY MANAGEMENT ==========

    @GetMapping("/categories")
    public String listCategories(Model model) {
        List<TutorialCategoryDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("title", "Manage Categories");
        return "admin/categories";
    }

    @GetMapping("/categories/new")
    public String newCategoryForm(Model model) {
        model.addAttribute("category", new TutorialCategoryDTO());
        model.addAttribute("allCategories", categoryService.getAllCategories());
        model.addAttribute("isEdit", false);
        model.addAttribute("title", "Add Category");
        return "admin/category-form";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        TutorialCategoryDTO category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);
        model.addAttribute("allCategories", categoryService.getAllCategories());
        model.addAttribute("isEdit", true);
        model.addAttribute("title", "Edit Category");
        return "admin/category-form";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute TutorialCategoryDTO category) {
        if (category.getId() == null) {
            categoryService.createCategory(category);
        } else {
            categoryService.updateCategory(category.getId(), category);
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/admin/categories";
    }

    // ========== TUTORIAL MANAGEMENT ==========

    @GetMapping("/tutorials")
    public String listTutorials(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<TutorialDTO> tutorials = tutorialService.getAllPublishedTutorials(page, 20, "id", "desc");
        model.addAttribute("tutorials", tutorials);
        model.addAttribute("title", "Manage Tutorials");
        return "admin/tutorials";
    }

    @GetMapping("/tutorials/new")
    public String newTutorialForm(Model model) {
        model.addAttribute("tutorial", new TutorialDTO());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEdit", false);
        model.addAttribute("title", "Add Tutorial");
        return "admin/tutorial-form";
    }

    @GetMapping("/tutorials/edit/{id}")
    public String editTutorialForm(@PathVariable Long id, Model model) {
        TutorialDTO tutorial = tutorialService.getTutorialById(id);
        model.addAttribute("tutorial", tutorial);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEdit", true);
        model.addAttribute("title", "Edit Tutorial");
        return "admin/tutorial-form";
    }

    @PostMapping("/tutorials/save")
    public String saveTutorial(@ModelAttribute TutorialDTO tutorial) {
        if (tutorial.getId() == null) {
            tutorialService.createTutorial(tutorial);
        } else {
            tutorialService.updateTutorial(tutorial.getId(), tutorial);
        }
        return "redirect:/admin/tutorials";
    }

    @PostMapping("/tutorials/publish/{id}")
    public String publishTutorial(@PathVariable Long id) {
        tutorialService.publishTutorial(id);
        return "redirect:/admin/tutorials";
    }

    @PostMapping("/tutorials/unpublish/{id}")
    public String unpublishTutorial(@PathVariable Long id) {
        tutorialService.unpublishTutorial(id);
        return "redirect:/admin/tutorials";
    }

    @PostMapping("/tutorials/delete/{id}")
    public String deleteTutorial(@PathVariable Long id) {
        tutorialService.deleteTutorial(id);
        return "redirect:/admin/tutorials";
    }

    // ========== QUIZ MANAGEMENT ==========

    @GetMapping("/quizzes")
    public String listQuizzes(Model model) {
        List<QuizDTO> quizzes = quizService.getAllQuizzes();
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("title", "Manage Quizzes");
        return "admin/quizzes";
    }

    @GetMapping("/quizzes/new")
    public String newQuizForm(@RequestParam(required = false) Long tutorialId, Model model) {
        QuizDTO quiz = new QuizDTO();
        if (tutorialId != null) {
            quiz.setTutorialId(tutorialId);
        }
        model.addAttribute("quiz", quiz);
        model.addAttribute("tutorials", tutorialService.getAllPublishedTutorials(0, 1000, "title", "asc").getContent());
        model.addAttribute("isEdit", false);
        model.addAttribute("title", "Add Quiz");
        return "admin/quiz-form";
    }

    @GetMapping("/quizzes/edit/{id}")
    public String editQuizForm(@PathVariable Long id, Model model) {
        QuizDTO quiz = quizService.getQuizById(id);
        model.addAttribute("quiz", quiz);
        model.addAttribute("tutorials", tutorialService.getAllPublishedTutorials(0, 1000, "title", "asc").getContent());
        model.addAttribute("isEdit", true);
        model.addAttribute("title", "Edit Quiz");
        return "admin/quiz-form";
    }

    @PostMapping("/quizzes/save")
    public String saveQuiz(@ModelAttribute QuizDTO quiz) {
        quizService.saveQuiz(quiz);
        return "redirect:/admin/quizzes";
    }

    @PostMapping("/quizzes/delete/{id}")
    public String deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return "redirect:/admin/quizzes";
    }

    // ========== QUESTION MANAGEMENT ==========

    @GetMapping("/quizzes/{quizId}/questions")
    public String listQuestions(@PathVariable Long quizId, Model model) {
        QuizDTO quiz = quizService.getQuizById(quizId);
        model.addAttribute("quiz", quiz);
        model.addAttribute("title", "Manage Questions - " + quiz.getTitle());
        return "admin/questions";
    }

    @GetMapping("/quizzes/{quizId}/questions/new")
    public String newQuestionForm(@PathVariable Long quizId, Model model) {
        QuestionDTO question = new QuestionDTO();
        question.setQuestionType("MULTIPLE_CHOICE");
        question.setQuizId(quizId);
        
        // Pre-populate with 4 empty options for better UX
        List<QuestionOptionDTO> options = new java.util.ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            options.add(QuestionOptionDTO.builder()
                .displayOrder(i)
                .correct(i == 1) // Default first one as correct
                .build());
        }
        question.setOptions(options);
        
        model.addAttribute("question", question);
        model.addAttribute("quizId", quizId);
        model.addAttribute("isEdit", false);
        model.addAttribute("title", "Add Question");
        return "admin/question-form";
    }

    @GetMapping("/questions/edit/{id}")
    public String editQuestionForm(@PathVariable Long id, Model model) {
        QuestionDTO question = quizService.getQuestionById(id);
        model.addAttribute("question", question);
        model.addAttribute("quizId", question.getQuizId());
        model.addAttribute("isEdit", true);
        model.addAttribute("title", "Edit Question");
        return "admin/question-form";
    }

    @PostMapping("/quizzes/{quizId}/questions/save")
    public String saveQuestion(@PathVariable Long quizId, @ModelAttribute QuestionDTO question) {
        if (question.getId() == null) {
            quizService.addQuestion(quizId, question);
        } else {
            quizService.updateQuestion(question.getId(), question);
        }
        return "redirect:/admin/quizzes/" + quizId + "/questions";
    }

    @PostMapping("/questions/delete/{id}")
    public String deleteQuestion(@PathVariable Long id, @RequestParam Long quizId) {
        quizService.deleteQuestion(id);
        return "redirect:/admin/quizzes/" + quizId + "/questions";
    }

    // ========== OPTION MANAGEMENT ==========

    @PostMapping("/questions/{questionId}/options/save")
    public String saveOption(@PathVariable Long questionId, @ModelAttribute QuestionOptionDTO option, @RequestParam Long quizId) {
        quizService.saveOption(questionId, option);
        return "redirect:/admin/questions/edit/" + questionId + "?quizId=" + quizId;
    }

    @PostMapping("/options/delete/{id}")
    public String deleteOption(@PathVariable Long id, @RequestParam Long questionId, @RequestParam Long quizId) {
        quizService.deleteOption(id);
        return "redirect:/admin/questions/edit/" + questionId + "?quizId=" + quizId;
    }
}
