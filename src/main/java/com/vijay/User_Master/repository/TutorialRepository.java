package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.Tutorial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutorialRepository extends JpaRepository<Tutorial, Long> {

    Optional<Tutorial> findBySlug(String slug);

    Page<Tutorial> findByIsPublishedTrue(Pageable pageable);

    Page<Tutorial> findByCategoryIdAndIsPublishedTrue(Long categoryId, Pageable pageable);

    Page<Tutorial> findByDifficultyAndIsPublishedTrue(String difficulty, Pageable pageable);

    Page<Tutorial> findByAuthorIdAndIsPublishedTrue(Long authorId, Pageable pageable);

    Page<Tutorial> findByAuthorId(Long authorId, Pageable pageable);

    @Query("SELECT t FROM Tutorial t WHERE t.isPublished = true " +
           "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.keywords) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Tutorial> searchPublishedTutorials(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT t FROM Tutorial t WHERE t.isPublished = true ORDER BY t.viewCount DESC")
    Page<Tutorial> findPopularTutorials(Pageable pageable);

    @Query("SELECT t FROM Tutorial t WHERE t.isPublished = true ORDER BY t.publishedAt DESC")
    Page<Tutorial> findRecentTutorials(Pageable pageable);

    @Query("SELECT t FROM Tutorial t WHERE t.category.id = :categoryId " +
           "AND t.isPublished = true ORDER BY t.displayOrder ASC")
    List<Tutorial> findByCategoryIdOrderByDisplayOrder(@Param("categoryId") Long categoryId);

    boolean existsBySlug(String slug);

    @Query("SELECT COUNT(t) FROM Tutorial t WHERE t.category.id = :categoryId AND t.isPublished = true")
    Long countPublishedTutorialsByCategory(@Param("categoryId") Long categoryId);
}
