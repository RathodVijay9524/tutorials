package com.vijay.User_Master.controller;

import com.vijay.User_Master.dto.tutorial.CommentDTO;
import com.vijay.User_Master.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    /**
     * Get all comments for a tutorial
     */
    @GetMapping("/tutorial/{tutorialId}")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long tutorialId) {
        try {
            List<CommentDTO> comments = commentService.getCommentsForTutorial(tutorialId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("Error fetching comments for tutorial {}: {}", tutorialId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add a new comment to a tutorial
     */
    @PostMapping("/tutorial/{tutorialId}")
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long tutorialId,
            @RequestBody Map<String, Object> request) {
        try {
            String content = (String) request.get("content");
            Long parentId = request.get("parentId") != null ? 
                    Long.parseLong(request.get("parentId").toString()) : null;

            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            CommentDTO comment = commentService.addComment(tutorialId, content, parentId);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            log.error("Error adding comment to tutorial {}: {}", tutorialId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update a comment
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            CommentDTO comment = commentService.updateComment(commentId, content);
            return ResponseEntity.ok(comment);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("Error updating comment {}: {}", commentId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete a comment
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("Error deleting comment {}: {}", commentId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get comment count for a tutorial
     */
    @GetMapping("/tutorial/{tutorialId}/count")
    public ResponseEntity<Map<String, Long>> getCommentCount(@PathVariable Long tutorialId) {
        try {
            long count = commentService.getCommentCount(tutorialId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            log.error("Error getting comment count for tutorial {}: {}", tutorialId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
