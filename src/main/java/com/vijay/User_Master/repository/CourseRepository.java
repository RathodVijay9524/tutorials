package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findBySlug(String slug);
    Page<Course> findByCategory_Id(Long categoryId, Pageable pageable);
    Page<Course> findByIsPublishedTrue(Pageable pageable);
    Page<Course> findByCategory_IdAndIsPublishedTrue(Long categoryId, Pageable pageable);
}
