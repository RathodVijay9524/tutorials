package com.vijay.User_Master.controller;

import com.vijay.User_Master.dto.tutorial.BookmarkDTO;
import com.vijay.User_Master.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
@Tag(name = "Bookmarks", description = "Tutorial bookmarking endpoints")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "Get all bookmarks for current user")
    @GetMapping
    public ResponseEntity<List<BookmarkDTO>> getMyBookmarks() {
        List<BookmarkDTO> bookmarks = bookmarkService.getMyBookmarks();
        return ResponseEntity.ok(bookmarks);
    }

    @Operation(summary = "Get bookmarks with pagination")
    @GetMapping("/paged")
    public ResponseEntity<Page<BookmarkDTO>> getMyBookmarksPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookmarkDTO> bookmarks = bookmarkService.getMyBookmarks(PageRequest.of(page, size));
        return ResponseEntity.ok(bookmarks);
    }

    @Operation(summary = "Add bookmark to a tutorial")
    @PostMapping("/tutorial/{tutorialId}")
    public ResponseEntity<BookmarkDTO> addBookmark(
            @PathVariable Long tutorialId,
            @RequestParam(required = false) String notes) {
        BookmarkDTO bookmark = bookmarkService.addBookmark(tutorialId, notes);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookmark);
    }

    @Operation(summary = "Remove bookmark from a tutorial")
    @DeleteMapping("/tutorial/{tutorialId}")
    public ResponseEntity<Void> removeBookmark(@PathVariable Long tutorialId) {
        bookmarkService.removeBookmark(tutorialId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Toggle bookmark status")
    @PostMapping("/tutorial/{tutorialId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleBookmark(@PathVariable Long tutorialId) {
        boolean isBookmarked = bookmarkService.toggleBookmark(tutorialId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("tutorialId", tutorialId);
        response.put("isBookmarked", isBookmarked);
        response.put("message", isBookmarked ? "Bookmark added" : "Bookmark removed");
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check if tutorial is bookmarked")
    @GetMapping("/tutorial/{tutorialId}/status")
    public ResponseEntity<Map<String, Object>> isBookmarked(@PathVariable Long tutorialId) {
        boolean isBookmarked = bookmarkService.isBookmarked(tutorialId);
        long bookmarkCount = bookmarkService.getTutorialBookmarkCount(tutorialId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("tutorialId", tutorialId);
        response.put("isBookmarked", isBookmarked);
        response.put("totalBookmarks", bookmarkCount);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update bookmark notes")
    @PutMapping("/{bookmarkId}/notes")
    public ResponseEntity<BookmarkDTO> updateNotes(
            @PathVariable Long bookmarkId,
            @RequestBody Map<String, String> request) {
        String notes = request.get("notes");
        BookmarkDTO bookmark = bookmarkService.updateNotes(bookmarkId, notes);
        return ResponseEntity.ok(bookmark);
    }

    @Operation(summary = "Get bookmark count for current user")
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getBookmarkCount() {
        long count = bookmarkService.getBookmarkCount();
        
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
}
