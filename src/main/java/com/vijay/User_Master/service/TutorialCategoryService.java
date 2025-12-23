package com.vijay.User_Master.service;

import com.vijay.User_Master.dto.tutorial.TutorialCategoryDTO;
import com.vijay.User_Master.entity.TutorialCategory;
import com.vijay.User_Master.repository.TutorialCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TutorialCategoryService {

    private final TutorialCategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<TutorialCategoryDTO> getAllCategories() {
        return categoryRepository.findAllOrderByDisplayOrder().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TutorialCategoryDTO> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TutorialCategoryDTO> getRootCategories() {
        return categoryRepository.findByParentIsNullAndIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TutorialCategoryDTO getCategoryById(Long id) {
        TutorialCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return convertToDTO(category);
    }

    @Transactional(readOnly = true)
    public TutorialCategoryDTO getCategoryBySlug(String slug) {
        TutorialCategory category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Category not found with slug: " + slug));
        return convertToDTO(category);
    }

    @Transactional
    public TutorialCategoryDTO createCategory(TutorialCategoryDTO categoryDTO) {
        if (categoryRepository.existsBySlug(categoryDTO.getSlug())) {
            throw new RuntimeException("Category with slug already exists: " + categoryDTO.getSlug());
        }

        TutorialCategory category = convertToEntity(categoryDTO);
        
        if (categoryDTO.getParentId() != null) {
            TutorialCategory parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParent(parent);
        }

        TutorialCategory saved = categoryRepository.save(category);
        log.info("Created category: {}", saved.getName());
        return convertToDTO(saved);
    }

    @Transactional
    public TutorialCategoryDTO updateCategory(Long id, TutorialCategoryDTO categoryDTO) {
        TutorialCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setIcon(categoryDTO.getIcon());
        category.setDisplayOrder(categoryDTO.getDisplayOrder());
        category.setActive(categoryDTO.isActive());

        if (categoryDTO.getParentId() != null && !categoryDTO.getParentId().equals(id)) {
            TutorialCategory parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParent(parent);
        }

        TutorialCategory updated = categoryRepository.save(category);
        log.info("Updated category: {}", updated.getName());
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteCategory(Long id) {
        TutorialCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        categoryRepository.delete(category);
        log.info("Deleted category: {}", category.getName());
    }

    private TutorialCategoryDTO convertToDTO(TutorialCategory category) {
        TutorialCategoryDTO dto = new TutorialCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setDescription(category.getDescription());
        dto.setIcon(category.getIcon());
        dto.setDisplayOrder(category.getDisplayOrder());
        dto.setActive(category.isActive());
        
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }
        
        dto.setTutorialCount(category.getTutorials() != null ? category.getTutorials().size() : 0);
        
        return dto;
    }

    private TutorialCategory convertToEntity(TutorialCategoryDTO dto) {
        TutorialCategory category = new TutorialCategory();
        category.setName(dto.getName());
        category.setSlug(dto.getSlug());
        category.setDescription(dto.getDescription());
        category.setIcon(dto.getIcon());
        category.setDisplayOrder(dto.getDisplayOrder());
        category.setActive(dto.isActive());
        return category;
    }
}
