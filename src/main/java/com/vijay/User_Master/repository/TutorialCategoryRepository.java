package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.TutorialCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutorialCategoryRepository extends JpaRepository<TutorialCategory, Long> {

    Optional<TutorialCategory> findBySlug(String slug);

    List<TutorialCategory> findByIsActiveTrue();

    List<TutorialCategory> findByParentIsNullAndIsActiveTrue();

    List<TutorialCategory> findByParentIdAndIsActiveTrue(Long parentId);

    @Query("SELECT c FROM TutorialCategory c WHERE c.parent.id = :parentId ORDER BY c.displayOrder ASC")
    List<TutorialCategory> findSubCategoriesByParentId(@Param("parentId") Long parentId);

    boolean existsBySlug(String slug);

    @Query("SELECT c FROM TutorialCategory c ORDER BY c.displayOrder ASC")
    List<TutorialCategory> findAllOrderByDisplayOrder();
}
