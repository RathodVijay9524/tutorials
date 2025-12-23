package com.vijay.User_Master.controller.view;

import com.vijay.User_Master.dto.tutorial.BookmarkDTO;
import com.vijay.User_Master.dto.tutorial.TutorialCategoryDTO;
import com.vijay.User_Master.dto.tutorial.TutorialDTO;
import com.vijay.User_Master.dto.tutorial.UserProgressDTO;
import com.vijay.User_Master.service.BookmarkService;
import com.vijay.User_Master.service.TutorialCategoryService;
import com.vijay.User_Master.service.TutorialService;
import com.vijay.User_Master.service.UserProgressService;
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
}

