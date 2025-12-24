package com.vijay.User_Master.service;

import com.vijay.User_Master.dto.tutorial.LessonCommentDTO;
import com.vijay.User_Master.entity.LessonComment;
import com.vijay.User_Master.entity.VideoLesson;
import com.vijay.User_Master.entity.User;
import com.vijay.User_Master.repository.LessonCommentRepository;
import com.vijay.User_Master.repository.VideoLessonRepository;
import com.vijay.User_Master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonCommentService {

    private final LessonCommentRepository commentRepository;
    private final VideoLessonRepository lessonRepository;
    private final UserRepository userRepository;

    /**
     * Get all comments for a lesson with replies
     */
    @Transactional(readOnly = true)
    public List<LessonCommentDTO> getCommentsForLesson(Long lessonId) {
        User currentUser = getCurrentUserOrNull();
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        
        List<LessonComment> topLevelComments = commentRepository.findTopLevelByLessonId(lessonId);
        
        return topLevelComments.stream()
                .map(comment -> convertToDTO(comment, currentUserId, true))
                .collect(Collectors.toList());
    }

    /**
     * Add a new comment to a lesson
     */
    @Transactional
    public LessonCommentDTO addComment(Long lessonId, String content, Long parentCommentId) {
        User user = getCurrentUser();
        VideoLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        LessonComment parent = null;
        if (parentCommentId != null) {
            parent = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
        }

        LessonComment comment = LessonComment.builder()
                .author(user)
                .lesson(lesson)
                .parentComment(parent)
                .content(content.trim())
                .build();

        comment = commentRepository.save(comment);
        log.info("User {} added comment to lesson {}", user.getUsername(), lesson.getTitle());

        return convertToDTO(comment, user.getId(), false);
    }

    /**
     * Update an existing comment
     */
    @Transactional
    public LessonCommentDTO updateComment(Long commentId, String content) {
        User user = getCurrentUser();
        LessonComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new SecurityException("Cannot edit another user's comment");
        }

        comment.setContent(content.trim());
        comment = commentRepository.save(comment);
        log.info("User {} updated comment {}", user.getUsername(), commentId);

        return convertToDTO(comment, user.getId(), false);
    }

    /**
     * Delete a comment
     */
    @Transactional
    public void deleteComment(Long commentId) {
        User user = getCurrentUser();
        LessonComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new SecurityException("Cannot delete another user's comment");
        }

        commentRepository.delete(comment);
        log.info("User {} deleted comment {}", user.getUsername(), commentId);
    }

    /**
     * Get comment count for a lesson
     */
    @Transactional(readOnly = true)
    public long getCommentCount(Long lessonId) {
        return commentRepository.countByLessonId(lessonId);
    }

    // ============ Private Helper Methods ============

    private LessonCommentDTO convertToDTO(LessonComment comment, Long currentUserId, boolean includeReplies) {
        List<LessonCommentDTO> replies = null;
        if (includeReplies) {
            List<LessonComment> replyEntities = commentRepository.findRepliesByParentId(comment.getId());
            replies = replyEntities.stream()
                    .map(reply -> convertToDTO(reply, currentUserId, false))
                    .collect(Collectors.toList());
        }

        return LessonCommentDTO.builder()
                .id(comment.getId())
                .lessonId(comment.getLesson().getId())
                .lessonTitle(comment.getLesson().getTitle())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getName())
                .authorImageName(comment.getAuthor().getImageName())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isEdited(comment.isEdited())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .canModify(currentUserId != null && currentUserId.equals(comment.getAuthor().getId()))
                .replies(replies != null ? replies : List.of())
                .build();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private User getCurrentUserOrNull() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if ("anonymousUser".equals(username)) {
                return null;
            }
            return userRepository.findByUsernameOrEmail(username, username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
