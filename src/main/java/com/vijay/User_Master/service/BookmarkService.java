package com.vijay.User_Master.service;

import com.vijay.User_Master.dto.tutorial.BookmarkDTO;
import com.vijay.User_Master.entity.Bookmark;
import com.vijay.User_Master.entity.Tutorial;
import com.vijay.User_Master.entity.User;
import com.vijay.User_Master.repository.BookmarkRepository;
import com.vijay.User_Master.repository.TutorialRepository;
import com.vijay.User_Master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final TutorialRepository tutorialRepository;
    private final UserRepository userRepository;

    /**
     * Add a bookmark for the current user
     */
    @Transactional
    public BookmarkDTO addBookmark(Long tutorialId, String notes) {
        User user = getCurrentUser();
        
        // Check if already bookmarked
        if (bookmarkRepository.existsByUserIdAndTutorialId(user.getId(), tutorialId)) {
            throw new IllegalStateException("Tutorial is already bookmarked");
        }
        
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new RuntimeException("Tutorial not found with id: " + tutorialId));
        
        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .tutorial(tutorial)
                .notes(notes)
                .build();
        
        bookmark = bookmarkRepository.save(bookmark);
        log.info("User {} bookmarked tutorial {}", user.getUsername(), tutorial.getTitle());
        
        return convertToDTO(bookmark);
    }

    /**
     * Remove a bookmark
     */
    @Transactional
    public void removeBookmark(Long tutorialId) {
        User user = getCurrentUser();
        
        if (!bookmarkRepository.existsByUserIdAndTutorialId(user.getId(), tutorialId)) {
            throw new RuntimeException("Bookmark not found");
        }
        
        bookmarkRepository.deleteByUserIdAndTutorialId(user.getId(), tutorialId);
        log.info("User {} removed bookmark for tutorial {}", user.getUsername(), tutorialId);
    }

    /**
     * Toggle bookmark (add if not exists, remove if exists)
     */
    @Transactional
    public boolean toggleBookmark(Long tutorialId) {
        User user = getCurrentUser();
        
        if (bookmarkRepository.existsByUserIdAndTutorialId(user.getId(), tutorialId)) {
            bookmarkRepository.deleteByUserIdAndTutorialId(user.getId(), tutorialId);
            log.info("User {} removed bookmark for tutorial {}", user.getUsername(), tutorialId);
            return false;
        } else {
            Tutorial tutorial = tutorialRepository.findById(tutorialId)
                    .orElseThrow(() -> new RuntimeException("Tutorial not found"));
            
            Bookmark bookmark = Bookmark.builder()
                    .user(user)
                    .tutorial(tutorial)
                    .build();
            
            bookmarkRepository.save(bookmark);
            log.info("User {} bookmarked tutorial {}", user.getUsername(), tutorialId);
            return true;
        }
    }

    /**
     * Check if current user has bookmarked a tutorial
     */
    public boolean isBookmarked(Long tutorialId) {
        try {
            User user = getCurrentUser();
            return bookmarkRepository.existsByUserIdAndTutorialId(user.getId(), tutorialId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get all bookmarks for current user
     */
    public List<BookmarkDTO> getMyBookmarks() {
        User user = getCurrentUser();
        return bookmarkRepository.findByUserIdWithTutorialDetails(user.getId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get bookmarks with pagination
     */
    public Page<BookmarkDTO> getMyBookmarks(Pageable pageable) {
        User user = getCurrentUser();
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::convertToDTO);
    }

    /**
     * Update bookmark notes
     */
    @Transactional
    public BookmarkDTO updateNotes(Long bookmarkId, String notes) {
        User user = getCurrentUser();
        
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));
        
        if (!bookmark.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot update bookmark of another user");
        }
        
        bookmark.setNotes(notes);
        bookmark = bookmarkRepository.save(bookmark);
        
        return convertToDTO(bookmark);
    }

    /**
     * Get bookmark count for current user
     */
    public long getBookmarkCount() {
        User user = getCurrentUser();
        return bookmarkRepository.countByUserId(user.getId());
    }

    /**
     * Get bookmark count for a tutorial (popularity indicator)
     */
    public long getTutorialBookmarkCount(Long tutorialId) {
        return bookmarkRepository.countByTutorialId(tutorialId);
    }

    // Helper methods
    
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private BookmarkDTO convertToDTO(Bookmark bookmark) {
        return BookmarkDTO.builder()
                .id(bookmark.getId())
                .userId(bookmark.getUser().getId())
                .username(bookmark.getUser().getUsername())
                .tutorialId(bookmark.getTutorial().getId())
                .tutorialTitle(bookmark.getTutorial().getTitle())
                .tutorialSlug(bookmark.getTutorial().getSlug())
                .categoryName(bookmark.getTutorial().getCategory() != null ? 
                        bookmark.getTutorial().getCategory().getName() : null)
                .categorySlug(bookmark.getTutorial().getCategory() != null ? 
                        bookmark.getTutorial().getCategory().getSlug() : null)
                .notes(bookmark.getNotes())
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
}
