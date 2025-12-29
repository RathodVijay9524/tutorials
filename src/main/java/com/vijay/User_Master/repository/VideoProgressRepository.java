package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.VideoProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoProgressRepository extends JpaRepository<VideoProgress, Long> {

    // Find progress for a specific user and video
    Optional<VideoProgress> findByUserIdAndVideoLessonId(Long userId, Long videoLessonId);

    // Find all progress for a user in a course
    @Query("SELECT vp FROM VideoProgress vp WHERE vp.user.id = :userId " +
           "AND vp.videoLesson.course.id = :courseId ORDER BY vp.videoLesson.lessonOrder ASC")
    List<VideoProgress> findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    // Find completed videos for a user
    List<VideoProgress> findByUserIdAndIsCompletedTrueOrderByCompletedAtDesc(Long userId);

    // Find in-progress videos for a user
    List<VideoProgress> findByUserIdAndIsCompletedFalseOrderByLastWatchedAtDesc(Long userId);

    // Count completed videos in a course
    @Query("SELECT COUNT(vp) FROM VideoProgress vp WHERE vp.user.id = :userId " +
           "AND vp.videoLesson.course.id = :courseId AND vp.isCompleted = true")
    Long countCompletedVideosInCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);
}

