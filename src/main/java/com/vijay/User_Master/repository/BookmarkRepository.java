package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.Bookmark;
import com.vijay.User_Master.entity.Tutorial;
import com.vijay.User_Master.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // Find bookmark by user and tutorial
    Optional<Bookmark> findByUserAndTutorial(User user, Tutorial tutorial);
    
    Optional<Bookmark> findByUserIdAndTutorialId(Long userId, Long tutorialId);

    // Check if user has bookmarked a tutorial
    boolean existsByUserIdAndTutorialId(Long userId, Long tutorialId);

    // Get all bookmarks for a user
    List<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Page<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Get all bookmarks for a user with tutorial details
    @Query("SELECT b FROM Bookmark b JOIN FETCH b.tutorial t JOIN FETCH t.category WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Bookmark> findByUserIdWithTutorialDetails(@Param("userId") Long userId);

    // Count bookmarks for a user
    long countByUserId(Long userId);

    // Count how many users bookmarked a tutorial
    long countByTutorialId(Long tutorialId);

    // Delete bookmark by user and tutorial
    void deleteByUserIdAndTutorialId(Long userId, Long tutorialId);
}
