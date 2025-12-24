package com.vijay.User_Master.service.impl;

import com.vijay.User_Master.dto.tutorial.CourseDTO;
import com.vijay.User_Master.dto.tutorial.VideoLessonDTO;
import com.vijay.User_Master.entity.Course;
import com.vijay.User_Master.entity.TutorialCategory;
import com.vijay.User_Master.entity.User;
import com.vijay.User_Master.entity.VideoLesson;
import com.vijay.User_Master.repository.CourseRepository;
import com.vijay.User_Master.repository.TutorialCategoryRepository;
import com.vijay.User_Master.repository.UserRepository;
import com.vijay.User_Master.repository.VideoLessonRepository;
import com.vijay.User_Master.service.CourseService;
import com.vijay.User_Master.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final VideoLessonRepository lessonRepository;
    private final TutorialCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    private static final String UPLOAD_DIR = "uploads/videos/";

    @Override
    @Transactional
    public CourseDTO createCourse(CourseDTO courseDTO) {
        TutorialCategory category = categoryRepository.findById(courseDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        User author = userRepository.findById(userService.getCurrentUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = Course.builder()
                .title(courseDTO.getTitle())
                .slug(courseDTO.getSlug())
                .description(courseDTO.getDescription())
                .thumbnailUrl(courseDTO.getThumbnailUrl())
                .difficulty(courseDTO.getDifficulty())
                .category(category)
                .author(author)
                .isPublished(false)
                .build();

        Course savedCourse = courseRepository.save(course);
        return mapToDTO(savedCourse);
    }

    @Override
    @Transactional
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        TutorialCategory category = categoryRepository.findById(courseDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        course.setTitle(courseDTO.getTitle());
        course.setSlug(courseDTO.getSlug());
        course.setDescription(courseDTO.getDescription());
        course.setThumbnailUrl(courseDTO.getThumbnailUrl());
        course.setDifficulty(courseDTO.getDifficulty());
        course.setCategory(category);

        Course updatedCourse = courseRepository.save(course);
        return mapToDTO(updatedCourse);
    }

    @Override
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return mapToDTO(course);
    }

    @Override
    public CourseDTO getCourseBySlug(String slug) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return mapToDTO(course);
    }

    @Override
    public Page<CourseDTO> getAllCourses(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return courseRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    public Page<CourseDTO> getPublishedCourses(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return courseRepository.findByIsPublishedTrue(pageable).map(this::mapToDTO);
    }

    @Override
    public Page<CourseDTO> getCoursesByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return courseRepository.findByCategory_Id(categoryId, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void publishCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        course.setPublished(true);
        courseRepository.save(course);
    }

    @Override
    @Transactional
    public void unpublishCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        course.setPublished(false);
        courseRepository.save(course);
    }

    @Override
    @Transactional
    public VideoLessonDTO addLesson(Long courseId, VideoLessonDTO lessonDTO) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        String videoUrl = lessonDTO.getVideoUrl();
        if (lessonDTO.getVideoFile() != null && !lessonDTO.getVideoFile().isEmpty()) {
            videoUrl = saveVideoFile(lessonDTO.getVideoFile());
        }

        VideoLesson lesson = VideoLesson.builder()
                .title(lessonDTO.getTitle())
                .slug(lessonDTO.getSlug())
                .videoUrl(videoUrl)
                .durationSeconds(lessonDTO.getDurationSeconds())
                .lessonOrder(lessonDTO.getLessonOrder())
                .summary(lessonDTO.getSummary())
                .course(course)
                .build();

        VideoLesson savedLesson = lessonRepository.save(lesson);
        return mapToLessonDTO(savedLesson);
    }

    @Override
    @Transactional
    public VideoLessonDTO updateLesson(Long lessonId, VideoLessonDTO lessonDTO) {
        VideoLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        if (lessonDTO.getVideoFile() != null && !lessonDTO.getVideoFile().isEmpty()) {
            lesson.setVideoUrl(saveVideoFile(lessonDTO.getVideoFile()));
        } else if (lessonDTO.getVideoUrl() != null) {
            lesson.setVideoUrl(lessonDTO.getVideoUrl());
        }

        lesson.setTitle(lessonDTO.getTitle());
        lesson.setSlug(lessonDTO.getSlug());
        lesson.setDurationSeconds(lessonDTO.getDurationSeconds());
        lesson.setLessonOrder(lessonDTO.getLessonOrder());
        lesson.setSummary(lessonDTO.getSummary());

        VideoLesson updatedLesson = lessonRepository.save(lesson);
        return mapToLessonDTO(updatedLesson);
    }

    private String saveVideoFile(org.springframework.web.multipart.MultipartFile file) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return "/videos/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not save video file", e);
        }
    }

    @Override
    public VideoLessonDTO getLessonById(Long id) {
        VideoLesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        return mapToLessonDTO(lesson);
    }

    @Override
    public VideoLessonDTO getLessonBySlug(String courseSlug, String lessonSlug) {
        VideoLesson lesson = lessonRepository.findByCourse_SlugAndSlug(courseSlug, lessonSlug)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        return mapToLessonDTO(lesson);
    }

    @Override
    public List<VideoLessonDTO> getLessonsByCourseId(Long courseId) {
        return lessonRepository.findByCourse_IdOrderByLessonOrderAsc(courseId)
                .stream()
                .map(this::mapToLessonDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        lessonRepository.deleteById(id);
    }

    // Helper methods
    private CourseDTO mapToDTO(Course course) {
        return CourseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .slug(course.getSlug())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .difficulty(course.getDifficulty())
                .published(course.isPublished())
                .categoryId(course.getCategory().getId())
                .categoryName(course.getCategory().getName())
                .authorId(course.getAuthor().getId())
                .authorName(course.getAuthor().getName())
                .createdAt(course.getCreatedAt())
                .lessons(course.getLessons() != null ? 
                        course.getLessons().stream().map(this::mapToLessonDTO).collect(Collectors.toList()) : null)
                .build();
    }

    private VideoLessonDTO mapToLessonDTO(VideoLesson lesson) {
        return VideoLessonDTO.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .slug(lesson.getSlug())
                .videoUrl(lesson.getVideoUrl())
                .durationSeconds(lesson.getDurationSeconds())
                .lessonOrder(lesson.getLessonOrder())
                .summary(lesson.getSummary())
                .courseId(lesson.getCourse().getId())
                .courseTitle(lesson.getCourse().getTitle())
                .build();
    }
}
