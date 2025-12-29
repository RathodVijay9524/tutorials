package com.vijay.User_Master.service;

import com.vijay.User_Master.dto.tutorial.VideoProgressDTO;
import com.vijay.User_Master.entity.User;
import com.vijay.User_Master.entity.VideoLesson;
import com.vijay.User_Master.entity.VideoProgress;
import com.vijay.User_Master.repository.UserRepository;
import com.vijay.User_Master.repository.VideoLessonRepository;
import com.vijay.User_Master.repository.VideoProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProgressService {

    private final VideoProgressRepository videoProgressRepository;
    private final VideoLessonRepository videoLessonRepository;
    private final UserRepository userRepository;

    /**
     * Update video progress (watch time and position)
     */
    @Transactional
    public VideoProgressDTO updateProgress(Long videoLessonId, Integer currentPositionSeconds) {
        User user = getCurrentUser();
        VideoLesson lesson = videoLessonRepository.findById(videoLessonId)
                .orElseThrow(() -> new RuntimeException("Video lesson not found"));

        Optional<VideoProgress> existing = videoProgressRepository
                .findByUserIdAndVideoLessonId(user.getId(), videoLessonId);

        VideoProgress progress;
        if (existing.isPresent()) {
            progress = existing.get();
        } else {
            progress = VideoProgress.builder()
                    .user(user)
                    .videoLesson(lesson)
                    .build();
        }

        // Update progress
        int videoDuration = lesson.getDurationSeconds() != null ? lesson.getDurationSeconds() : 0;
        progress.updateProgress(currentPositionSeconds, videoDuration);

        progress = videoProgressRepository.save(progress);
        log.debug("Updated video progress for user {} on lesson {}: {}%", 
                user.getUsername(), lesson.getTitle(), progress.getCompletionPercentage());

        return convertToDTO(progress);
    }

    /**
     * Get video progress for a specific lesson
     */
    @Transactional(readOnly = true)
    public VideoProgressDTO getProgress(Long videoLessonId) {
        User user = getCurrentUser();
        Optional<VideoProgress> progress = videoProgressRepository
                .findByUserIdAndVideoLessonId(user.getId(), videoLessonId);

        return progress.map(this::convertToDTO).orElse(null);
    }

    /**
     * Get all progress for a course
     */
    @Transactional(readOnly = true)
    public List<VideoProgressDTO> getCourseProgress(Long courseId) {
        User user = getCurrentUser();
        List<VideoProgress> progressList = videoProgressRepository
                .findByUserIdAndCourseId(user.getId(), courseId);

        return progressList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Mark video as completed
     */
    @Transactional
    public VideoProgressDTO markAsCompleted(Long videoLessonId) {
        User user = getCurrentUser();
        VideoProgress progress = videoProgressRepository
                .findByUserIdAndVideoLessonId(user.getId(), videoLessonId)
                .orElseThrow(() -> new RuntimeException("Video progress not found"));

        VideoLesson lesson = progress.getVideoLesson();
        int duration = lesson.getDurationSeconds() != null ? lesson.getDurationSeconds() : 0;
        progress.updateProgress(duration, duration); // Set to 100%

        progress = videoProgressRepository.save(progress);
        log.info("User {} marked video {} as completed", user.getUsername(), lesson.getTitle());

        return convertToDTO(progress);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }
        return user;
    }

    private VideoProgressDTO convertToDTO(VideoProgress progress) {
        return VideoProgressDTO.builder()
                .id(progress.getId())
                .userId(progress.getUser().getId())
                .videoLessonId(progress.getVideoLesson().getId())
                .watchTimeSeconds(progress.getWatchTimeSeconds())
                .lastPositionSeconds(progress.getLastPositionSeconds())
                .isCompleted(progress.isCompleted())
                .completionPercentage(progress.getCompletionPercentage())
                .lastWatchedAt(progress.getLastWatchedAt())
                .completedAt(progress.getCompletedAt())
                .build();
    }
}

