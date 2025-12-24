package com.vijay.User_Master.service;

import com.vijay.User_Master.dto.tutorial.CourseDTO;
import com.vijay.User_Master.dto.tutorial.VideoLessonDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CourseService {
    // Course management
    CourseDTO createCourse(CourseDTO courseDTO);
    CourseDTO updateCourse(Long id, CourseDTO courseDTO);
    CourseDTO getCourseById(Long id);
    CourseDTO getCourseBySlug(String slug);
    Page<CourseDTO> getAllCourses(int page, int size, String sortBy, String sortDir);
    Page<CourseDTO> getPublishedCourses(int page, int size, String sortBy, String sortDir);
    Page<CourseDTO> getCoursesByCategory(Long categoryId, int page, int size);
    void deleteCourse(Long id);
    void publishCourse(Long id);
    void unpublishCourse(Long id);

    // Video Lesson management
    VideoLessonDTO addLesson(Long courseId, VideoLessonDTO lessonDTO);
    VideoLessonDTO updateLesson(Long lessonId, VideoLessonDTO lessonDTO);
    VideoLessonDTO getLessonById(Long id);
    VideoLessonDTO getLessonBySlug(String courseSlug, String lessonSlug);
    List<VideoLessonDTO> getLessonsByCourseId(Long courseId);
    void deleteLesson(Long id);
}
