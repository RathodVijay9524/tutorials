package com.vijay.User_Master.controller;

import com.vijay.User_Master.dto.tutorial.TutorialCategoryDTO;
import com.vijay.User_Master.service.TutorialCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Tutorial Categories", description = "Tutorial category management APIs")
public class TutorialCategoryController {

    private final TutorialCategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve all tutorial categories")
    public ResponseEntity<List<TutorialCategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/active")
    @Operation(summary = "Get active categories", description = "Retrieve only active categories")
    public ResponseEntity<List<TutorialCategoryDTO>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.getActiveCategories());
    }

    @GetMapping("/root")
    @Operation(summary = "Get root categories", description = "Retrieve top-level categories without parent")
    public ResponseEntity<List<TutorialCategoryDTO>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve a specific category by its ID")
    public ResponseEntity<TutorialCategoryDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug", description = "Retrieve a category by its URL slug")
    public ResponseEntity<TutorialCategoryDTO> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_USER')")
    @Operation(summary = "Create category", description = "Create a new tutorial category (Admin/SuperUser only)")
    public ResponseEntity<TutorialCategoryDTO> createCategory(@RequestBody TutorialCategoryDTO categoryDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(categoryDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_USER')")
    @Operation(summary = "Update category", description = "Update an existing category (Admin/SuperUser only)")
    public ResponseEntity<TutorialCategoryDTO> updateCategory(
            @PathVariable Long id,
            @RequestBody TutorialCategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete category", description = "Delete a category (Admin only)")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
