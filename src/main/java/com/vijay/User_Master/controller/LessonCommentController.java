package com.vijay.User_Master.controller;

import com.vijay.User_Master.dto.tutorial.LessonCommentDTO;
import com.vijay.User_Master.service.LessonCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lesson-comments")
@RequiredArgsConstructor
@Slf4j
public class LessonCommentController {

    private final LessonCommentService commentService;

    /**
     * Get all comments for a lesson
     */
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<LessonCommentDTO>> getComments(@PathVariable Long lessonId) {
        try {
            List<LessonCommentDTO> comments = commentService.getCommentsForLesson(lessonId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("Error fetching comments for lesson {}: {}", lessonId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add a new comment to a lesson
     */
    @PostMapping("/lesson/{lessonId}")
    public ResponseEntity<LessonCommentDTO> addComment(
            @PathVariable Long lessonId,
            @RequestBody Map<String, Object> request) {
        try {
            String content = (String) request.get("content");
            Long parentId = request.get("parentId") != null ? 
                    Long.parseLong(request.get("parentId").toString()) : null;

            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            LessonCommentDTO comment = commentService.addComment(lessonId, content, parentId);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            log.error("Error adding comment to lesson {}: {}", lessonId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update a comment
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<LessonCommentDTO> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            LessonCommentDTO comment = commentService.updateComment(commentId, content);
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
     * Get comment count for a lesson
     */
    @GetMapping("/lesson/{lessonId}/count")
    public ResponseEntity<Map<String, Long>> getCommentCount(@PathVariable Long lessonId) {
        try {
            long count = commentService.getCommentCount(lessonId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            log.error("Error getting comment count for lesson {}: {}", lessonId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
