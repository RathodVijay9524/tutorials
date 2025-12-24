package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.VideoLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoLessonRepository extends JpaRepository<VideoLesson, Long> {
    List<VideoLesson> findByCourse_IdOrderByLessonOrderAsc(Long courseId);
    Optional<VideoLesson> findByCourse_SlugAndSlug(String courseSlug, String lessonSlug);
}
