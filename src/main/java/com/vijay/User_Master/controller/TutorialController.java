package com.vijay.User_Master.controller;

import com.vijay.User_Master.dto.tutorial.TutorialDTO;
import com.vijay.User_Master.service.TutorialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tutorials")
@RequiredArgsConstructor
@Tag(name = "Tutorials", description = "Tutorial management and browsing APIs")
public class TutorialController {

    private final TutorialService tutorialService;

    @GetMapping
    @Operation(summary = "Get all published tutorials", description = "Retrieve paginated list of published tutorials")
    public ResponseEntity<Page<TutorialDTO>> getAllTutorials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(tutorialService.getAllPublishedTutorials(page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tutorial by ID", description = "Retrieve a specific tutorial by its ID")
    public ResponseEntity<TutorialDTO> getTutorialById(@PathVariable Long id) {
        return ResponseEntity.ok(tutorialService.getTutorialById(id));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get tutorial by slug", description = "Retrieve a tutorial by its URL slug and increment view count")
    public ResponseEntity<TutorialDTO> getTutorialBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(tutorialService.getTutorialBySlug(slug));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get tutorials by category", description = "Retrieve tutorials belonging to a specific category")
    public ResponseEntity<Page<TutorialDTO>> getTutorialsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(tutorialService.getTutorialsByCategory(categoryId, page, size));
    }

    @GetMapping("/difficulty/{difficulty}")
    @Operation(summary = "Get tutorials by difficulty", description = "Filter tutorials by difficulty level (BEGINNER, INTERMEDIATE, ADVANCED)")
    public ResponseEntity<Page<TutorialDTO>> getTutorialsByDifficulty(
            @PathVariable String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(tutorialService.getTutorialsByDifficulty(difficulty, page, size));
    }

    @GetMapping("/search")
    @Operation(summary = "Search tutorials", description = "Search tutorials by keyword in title, content, or keywords")
    public ResponseEntity<Page<TutorialDTO>> searchTutorials(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(tutorialService.searchTutorials(keyword, page, size));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular tutorials", description = "Retrieve most viewed tutorials")
    public ResponseEntity<Page<TutorialDTO>> getPopularTutorials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(tutorialService.getPopularTutorials(page, size));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent tutorials", description = "Retrieve recently published tutorials")
    public ResponseEntity<Page<TutorialDTO>> getRecentTutorials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(tutorialService.getRecentTutorials(page, size));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_USER')")
    @Operation(summary = "Create tutorial", description = "Create a new tutorial (Admin/SuperUser only)")
    public ResponseEntity<TutorialDTO> createTutorial(@RequestBody TutorialDTO tutorialDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tutorialService.createTutorial(tutorialDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_USER')")
    @Operation(summary = "Update tutorial", description = "Update an existing tutorial (Admin/SuperUser only)")
    public ResponseEntity<TutorialDTO> updateTutorial(
            @PathVariable Long id,
            @RequestBody TutorialDTO tutorialDTO) {
        return ResponseEntity.ok(tutorialService.updateTutorial(id, tutorialDTO));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_USER')")
    @Operation(summary = "Publish tutorial", description = "Publish a tutorial to make it visible to users")
    public ResponseEntity<Void> publishTutorial(@PathVariable Long id) {
        tutorialService.publishTutorial(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_USER')")
    @Operation(summary = "Unpublish tutorial", description = "Unpublish a tutorial to hide it from users")
    public ResponseEntity<Void> unpublishTutorial(@PathVariable Long id) {
        tutorialService.unpublishTutorial(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete tutorial", description = "Delete a tutorial (Admin only)")
    public ResponseEntity<Void> deleteTutorial(@PathVariable Long id) {
        tutorialService.deleteTutorial(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/export/pdf")
    @Operation(summary = "Export tutorial as PDF", description = "Generate and download a PDF version of the tutorial")
    public ResponseEntity<byte[]> exportTutorialAsPdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = tutorialService.exportTutorialAsPdf(id);
            TutorialDTO tutorial = tutorialService.getTutorialById(id);
            
            String filename = tutorial.getSlug() != null 
                ? tutorial.getSlug() + ".pdf" 
                : "tutorial-" + id + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
