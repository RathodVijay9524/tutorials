package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.LessonComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonCommentRepository extends JpaRepository<LessonComment, Long> {

    // Find top-level comments for a lesson (no parent)
    @Query("SELECT c FROM LessonComment c JOIN FETCH c.author WHERE c.lesson.id = :lessonId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    List<LessonComment> findTopLevelByLessonId(@Param("lessonId") Long lessonId);

    // Find replies for a comment
    @Query("SELECT c FROM LessonComment c JOIN FETCH c.author WHERE c.parentComment.id = :parentId ORDER BY c.createdAt ASC")
    List<LessonComment> findRepliesByParentId(@Param("parentId") Long parentId);

    // Count comments for a lesson
    long countByLessonId(Long lessonId);

    // Find paginated comments
    Page<LessonComment> findByLessonIdAndParentCommentIsNullOrderByCreatedAtDesc(Long lessonId, Pageable pageable);

    // Find all comments by user
    List<LessonComment> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    // Count replies for a comment
    long countByParentCommentId(Long parentCommentId);
}
