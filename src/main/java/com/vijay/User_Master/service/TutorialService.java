package com.vijay.User_Master.service;

import com.vijay.User_Master.dto.tutorial.TutorialDTO;
import com.vijay.User_Master.dto.tutorial.CodeSnippetDTO;
import com.vijay.User_Master.entity.Tutorial;
import com.vijay.User_Master.entity.TutorialCategory;
import com.vijay.User_Master.entity.User;
import com.vijay.User_Master.repository.TutorialRepository;
import com.vijay.User_Master.repository.TutorialCategoryRepository;
import com.vijay.User_Master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TutorialService {

    private final TutorialRepository tutorialRepository;
    private final TutorialCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public Page<TutorialDTO> getAllPublishedTutorials(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return tutorialRepository.findByIsPublishedTrue(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<TutorialDTO> getAllTutorials(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return tutorialRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<TutorialDTO> getTutorialsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tutorialRepository.findByCategoryIdAndIsPublishedTrue(categoryId, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<TutorialDTO> getTutorialsByDifficulty(String difficulty, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tutorialRepository.findByDifficultyAndIsPublishedTrue(difficulty, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<TutorialDTO> searchTutorials(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tutorialRepository.searchPublishedTutorials(keyword, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<TutorialDTO> getPopularTutorials(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tutorialRepository.findPopularTutorials(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<TutorialDTO> getRecentTutorials(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tutorialRepository.findRecentTutorials(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public TutorialDTO getTutorialById(Long id) {
        Tutorial tutorial = tutorialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutorial not found with id: " + id));
        return convertToDTO(tutorial);
    }

    @Transactional
    public TutorialDTO getTutorialBySlug(String slug) {
        Tutorial tutorial = tutorialRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Tutorial not found with slug: " + slug));
        
        // Increment view count
        tutorial.incrementViewCount();
        tutorialRepository.save(tutorial);
        
        return convertToDTO(tutorial);
    }

    @Transactional
    public TutorialDTO createTutorial(TutorialDTO tutorialDTO) {
        if (tutorialRepository.existsBySlug(tutorialDTO.getSlug())) {
            throw new RuntimeException("Tutorial with slug already exists: " + tutorialDTO.getSlug());
        }

        Tutorial tutorial = convertToEntity(tutorialDTO);
        
        // Set category
        TutorialCategory category = categoryRepository.findById(tutorialDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        tutorial.setCategory(category);

        // Set author (current user)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByUsername(username);
        if (author == null) {
            throw new RuntimeException("Author not found");
        }
        tutorial.setAuthor(author);

        Tutorial saved = tutorialRepository.save(tutorial);
        log.info("Created tutorial: {}", saved.getTitle());
        return convertToDTO(saved);
    }

    @Transactional
    public TutorialDTO updateTutorial(Long id, TutorialDTO tutorialDTO) {
        Tutorial tutorial = tutorialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutorial not found with id: " + id));

        tutorial.setTitle(tutorialDTO.getTitle());
        tutorial.setContent(tutorialDTO.getContent());
        tutorial.setCodeExample(tutorialDTO.getCodeExample());
        tutorial.setDifficulty(tutorialDTO.getDifficulty());
        tutorial.setEstimatedMinutes(tutorialDTO.getEstimatedMinutes());
        tutorial.setDisplayOrder(tutorialDTO.getDisplayOrder());
        tutorial.setMetaTitle(tutorialDTO.getMetaTitle());
        tutorial.setMetaDescription(tutorialDTO.getMetaDescription());
        tutorial.setKeywords(tutorialDTO.getKeywords());

        if (tutorialDTO.getCategoryId() != null) {
            TutorialCategory category = categoryRepository.findById(tutorialDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            tutorial.setCategory(category);
        }

        Tutorial updated = tutorialRepository.save(tutorial);
        log.info("Updated tutorial: {}", updated.getTitle());
        return convertToDTO(updated);
    }

    @Transactional
    public void publishTutorial(Long id) {
        Tutorial tutorial = tutorialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutorial not found with id: " + id));
        
        tutorial.setPublished(true);
        tutorial.setPublishedAt(LocalDateTime.now());
        tutorialRepository.save(tutorial);
        log.info("Published tutorial: {}", tutorial.getTitle());
    }

    @Transactional
    public void unpublishTutorial(Long id) {
        Tutorial tutorial = tutorialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutorial not found with id: " + id));
        
        tutorial.setPublished(false);
        tutorialRepository.save(tutorial);
        log.info("Unpublished tutorial: {}", tutorial.getTitle());
    }

    @Transactional
    public void deleteTutorial(Long id) {
        Tutorial tutorial = tutorialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutorial not found with id: " + id));
        
        tutorialRepository.delete(tutorial);
        log.info("Deleted tutorial: {}", tutorial.getTitle());
    }

    private TutorialDTO convertToDTO(Tutorial tutorial) {
        TutorialDTO dto = new TutorialDTO();
        dto.setId(tutorial.getId());
        dto.setTitle(tutorial.getTitle());
        dto.setSlug(tutorial.getSlug());
        dto.setContent(tutorial.getContent());
        dto.setCodeExample(tutorial.getCodeExample());
        dto.setDifficulty(tutorial.getDifficulty());
        dto.setEstimatedMinutes(tutorial.getEstimatedMinutes());
        dto.setViewCount(tutorial.getViewCount() != null ? tutorial.getViewCount() : 0L);
        dto.setPublished(tutorial.isPublished());
        dto.setPublishedAt(tutorial.getPublishedAt());
        dto.setDisplayOrder(tutorial.getDisplayOrder());
        dto.setMetaTitle(tutorial.getMetaTitle());
        dto.setMetaDescription(tutorial.getMetaDescription());
        dto.setKeywords(tutorial.getKeywords());
        dto.setAverageRating(tutorial.getAverageRating() != null ? tutorial.getAverageRating() : 0.0);
        dto.setRatingCount(tutorial.getRatingCount() != null ? tutorial.getRatingCount() : 0);
        dto.setVideoUrl(tutorial.getVideoUrl());
        dto.setVideoDuration(tutorial.getVideoDuration());
        dto.setVideoThumbnail(tutorial.getVideoThumbnail());
        dto.setCreatedAt(tutorial.getCreatedAt());
        dto.setUpdatedAt(tutorial.getUpdatedAt());

        if (tutorial.getCategory() != null) {
            dto.setCategoryId(tutorial.getCategory().getId());
            dto.setCategoryName(tutorial.getCategory().getName());
        }
        
        if (tutorial.getAuthor() != null) {
            dto.setAuthorId(tutorial.getAuthor().getId());
            dto.setAuthorName(tutorial.getAuthor().getName());
        } else {
            dto.setAuthorName("Anonymous");
        }
        
        if (tutorial.getCodeSnippets() != null) {
            List<CodeSnippetDTO> snippets = tutorial.getCodeSnippets().stream()
                    .map(snippet -> {
                        CodeSnippetDTO sDto = new CodeSnippetDTO();
                        sDto.setId(snippet.getId());
                        sDto.setTitle(snippet.getTitle());
                        sDto.setCode(snippet.getCode());
                        sDto.setExpectedOutput(snippet.getExpectedOutput());
                        sDto.setExecutable(snippet.isExecutable());
                        sDto.setEditable(snippet.isEditable());
                        sDto.setDisplayOrder(snippet.getDisplayOrder());
                        return sDto;
                    })
                    .collect(Collectors.toList());
            dto.setCodeSnippets(snippets);
        }
        
        return dto;
    }

    private Tutorial convertToEntity(TutorialDTO dto) {
        Tutorial tutorial = new Tutorial();
        tutorial.setTitle(dto.getTitle());
        tutorial.setSlug(dto.getSlug());
        tutorial.setContent(dto.getContent());
        tutorial.setCodeExample(dto.getCodeExample());
        tutorial.setDifficulty(dto.getDifficulty());
        tutorial.setEstimatedMinutes(dto.getEstimatedMinutes());
        tutorial.setDisplayOrder(dto.getDisplayOrder());
        tutorial.setMetaTitle(dto.getMetaTitle());
        tutorial.setMetaDescription(dto.getMetaDescription());
        tutorial.setKeywords(dto.getKeywords());
        tutorial.setVideoUrl(dto.getVideoUrl());
        tutorial.setVideoDuration(dto.getVideoDuration());
        tutorial.setVideoThumbnail(dto.getVideoThumbnail());
        return tutorial;
    }
}
