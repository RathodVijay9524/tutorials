package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Find top-level comments for a tutorial (no parent)
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.tutorial.id = :tutorialId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findTopLevelByTutorialId(@Param("tutorialId") Long tutorialId);

    // Find replies for a comment
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.parentComment.id = :parentId ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);

    // Count comments for a tutorial
    long countByTutorialId(Long tutorialId);

    // Find paginated comments
    Page<Comment> findByTutorialIdAndParentCommentIsNullOrderByCreatedAtDesc(Long tutorialId, Pageable pageable);

    // Find all comments by user
    List<Comment> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    // Count replies for a comment
    long countByParentCommentId(Long parentCommentId);
}
