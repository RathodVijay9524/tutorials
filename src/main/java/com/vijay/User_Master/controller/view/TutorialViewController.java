package com.vijay.User_Master.controller.view;

import com.vijay.User_Master.dto.tutorial.*;
import com.vijay.User_Master.dto.UserResponse;
import com.vijay.User_Master.repository.UserProgressRepository;
import com.vijay.User_Master.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/tutorials")
@RequiredArgsConstructor
public class TutorialViewController {

    private final TutorialCategoryService categoryService;
    private final TutorialService tutorialService;
    private final UserProgressService progressService;
    private final BookmarkService bookmarkService;
    private final UserService userService;
    private final UserProgressRepository progressRepository;
    private final CourseService courseService;

    @GetMapping
    public String home(Model model) {
        List<TutorialCategoryDTO> rootCategories = categoryService.getRootCategories();
        model.addAttribute("categories", rootCategories);
        model.addAttribute("title", "Java Tutorial Platform - Home");
        return "tutorials/index";
    }

    @GetMapping("/category/{slug}")
    public String categoryTutorials(@PathVariable String slug, 
                                   @RequestParam(defaultValue = "0") int page,
                                   Model model) {
        TutorialCategoryDTO category = categoryService.getCategoryBySlug(slug);
        Page<TutorialDTO> tutorials = tutorialService.getTutorialsByCategory(category.getId(), page, 10);
        
        model.addAttribute("category", category);
        model.addAttribute("tutorials", tutorials);
        model.addAttribute("title", category.getName() + " Tutorials");
        return "tutorials/category-list";
    }

    @GetMapping("/view/{slug}")
    public String viewTutorial(@PathVariable String slug, Model model) {
        TutorialDTO tutorial = tutorialService.getTutorialBySlug(slug);
        
        // Check if bookmarked and get bookmark status
        boolean isBookmarked = false;
        try {
            isBookmarked = bookmarkService.isBookmarked(tutorial.getId());
            progressService.startTutorial(tutorial.getId());
        } catch (Exception e) {
            // User might not be logged in or error in progress/bookmark service
            System.err.println("Error in viewTutorial optional features: " + e.getMessage());
        }

        model.addAttribute("tutorial", tutorial);
        model.addAttribute("isBookmarked", isBookmarked);
        model.addAttribute("title", tutorial.getTitle());
        return "tutorials/detail";
    }

    @GetMapping("/playground")
    public String playground(Model model) {
        model.addAttribute("title", "Java Playground");
        return "tutorials/playground";
    }

    @GetMapping("/bookmarks")
    public String bookmarks(Model model) {
        try {
            List<BookmarkDTO> bookmarks = bookmarkService.getMyBookmarks();
            model.addAttribute("bookmarks", bookmarks);
        } catch (Exception e) {
            model.addAttribute("bookmarks", List.of());
        }
        model.addAttribute("title", "My Bookmarks");
        return "tutorials/bookmarks";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("title", "My Profile");
        return "tutorials/profile";
    }

    @GetMapping("/search")
    public String search(@RequestParam("q") String query,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Page<TutorialDTO> results = tutorialService.searchTutorials(query, page, 10);
        model.addAttribute("tutorials", results);
        model.addAttribute("query", query);
        model.addAttribute("title", "Search Results: " + query);
        return "tutorials/search";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            UserResponse user = userService.getCurrentUser();
            Long userId = user.getId();

            // 1. Basic Stats
            model.addAttribute("completedCount", progressService.getCompletedTutorialsCount(userId));
            model.addAttribute("avgProgress", progressService.getAverageProgress(userId));
            model.addAttribute("totalTime", progressService.getTotalTimeSpent(userId));

            // 2. Recent Activity (Last 5 progress updates)
            List<UserProgressDTO> recentActivity = progressRepository.findRecentProgressByUserId(userId).stream()
                    .limit(5)
                    .map(p -> UserProgressDTO.builder()
                            .tutorialId(p.getTutorial().getId())
                            .tutorialTitle(p.getTutorial().getTitle())
                            .progressPercentage(p.getProgressPercentage())
                            .lastAccessedAt(p.getLastAccessedAt())
                            .isCompleted(p.isCompleted())
                            .build())
                    .toList();
            model.addAttribute("recentActivity", recentActivity);

            // 3. Category Progress
            List<TutorialCategoryDTO> categories = categoryService.getActiveCategories();
            model.addAttribute("categoryStats", categories.stream().map(cat -> {
                long totalInCat = tutorialService.getTutorialsByCategory(cat.getId(), 0, 1000).getTotalElements();
                long completedInCat = progressRepository.findByUserIdAndIsCompletedTrue(userId).stream()
                        .filter(p -> p.getTutorial().getCategory().getId().equals(cat.getId()))
                        .count();
                
                double percent = totalInCat > 0 ? (completedInCat * 100.0 / totalInCat) : 0;
                
                return new Object() {
                    public String getName() { return cat.getName(); }
                    public String getIcon() { return cat.getIcon(); }
                    public long getTotal() { return totalInCat; }
                    public long getCompleted() { return completedInCat; }
                    public int getPercentage() { return (int) percent; }
                };
            }).toList());

            model.addAttribute("user", user);
            model.addAttribute("title", "Learning Dashboard");
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "tutorials/dashboard";
    }

    // ========== COURSE PUBLIC VIEW ==========

    @GetMapping("/courses")
    public String browseCourses(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<CourseDTO> courses = courseService.getPublishedCourses(page, 12, "id", "desc");
        model.addAttribute("courses", courses);
        model.addAttribute("title", "Online Courses - Video Tutorials");
        return "tutorials/courses";
    }

    @GetMapping("/courses/view/{slug}")
    public String viewCourse(@PathVariable String slug, Model model) {
        CourseDTO course = courseService.getCourseBySlug(slug);
        model.addAttribute("course", course);
        model.addAttribute("title", course.getTitle());
        return "tutorials/course-detail";
    }

    @GetMapping("/courses/{courseSlug}/watch/{lessonSlug}")
    public String watchLesson(@PathVariable String courseSlug, @PathVariable String lessonSlug, Model model) {
        CourseDTO course = courseService.getCourseBySlug(courseSlug);
        VideoLessonDTO lesson = courseService.getLessonBySlug(courseSlug, lessonSlug);
        
        model.addAttribute("course", course);
        model.addAttribute("lesson", lesson);
        model.addAttribute("title", lesson.getTitle() + " - " + course.getTitle());
        return "tutorials/watch-lesson";
    }
}

