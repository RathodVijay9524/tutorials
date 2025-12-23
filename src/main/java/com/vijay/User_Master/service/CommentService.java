package com.vijay.User_Master.service;

import com.vijay.User_Master.dto.tutorial.CommentDTO;
import com.vijay.User_Master.entity.Comment;
import com.vijay.User_Master.entity.Tutorial;
import com.vijay.User_Master.entity.User;
import com.vijay.User_Master.repository.CommentRepository;
import com.vijay.User_Master.repository.TutorialRepository;
import com.vijay.User_Master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final TutorialRepository tutorialRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");

    /**
     * Get all comments for a tutorial with replies
     */
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsForTutorial(Long tutorialId) {
        User currentUser = getCurrentUserOrNull();
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        
        List<Comment> topLevelComments = commentRepository.findTopLevelByTutorialId(tutorialId);
        
        return topLevelComments.stream()
                .map(comment -> convertToDTO(comment, currentUserId, true))
                .collect(Collectors.toList());
    }

    /**
     * Add a new comment to a tutorial
     */
    @Transactional
    public CommentDTO addComment(Long tutorialId, String content, Long parentCommentId) {
        User user = getCurrentUser();
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new RuntimeException("Tutorial not found"));

        Comment parent = null;
        if (parentCommentId != null) {
            parent = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
        }

        Comment comment = Comment.builder()
                .author(user)
                .tutorial(tutorial)
                .parentComment(parent)
                .content(content.trim())
                .build();

        comment = commentRepository.save(comment);
        log.info("User {} added comment to tutorial {}", user.getUsername(), tutorial.getTitle());

        return convertToDTO(comment, user.getId(), false);
    }

    /**
     * Update an existing comment
     */
    @Transactional
    public CommentDTO updateComment(Long commentId, String content) {
        User user = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
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
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new SecurityException("Cannot delete another user's comment");
        }

        commentRepository.delete(comment);
        log.info("User {} deleted comment {}", user.getUsername(), commentId);
    }

    /**
     * Get comment count for a tutorial
     */
    @Transactional(readOnly = true)
    public long getCommentCount(Long tutorialId) {
        return commentRepository.countByTutorialId(tutorialId);
    }

    // ============ Private Helper Methods ============

    private CommentDTO convertToDTO(Comment comment, Long currentUserId, boolean includeReplies) {
        List<CommentDTO> replies = null;
        if (includeReplies) {
            List<Comment> replyEntities = commentRepository.findRepliesByParentId(comment.getId());
            replies = replyEntities.stream()
                    .map(reply -> convertToDTO(reply, currentUserId, false))
                    .collect(Collectors.toList());
        }

        return CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorUsername(comment.getAuthor().getUsername())
                .authorId(comment.getAuthor().getId())
                .createdAt(comment.getCreatedAt().format(DATE_FORMATTER))
                .updatedAt(comment.getUpdatedAt().format(DATE_FORMATTER))
                .isEdited(comment.isEdited())
                .isOwner(currentUserId != null && currentUserId.equals(comment.getAuthor().getId()))
                .replyCount((int) commentRepository.countByParentCommentId(comment.getId()))
                .replies(replies)
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
