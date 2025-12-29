package com.vijay.User_Master.controller;

import com.vijay.User_Master.dto.tutorial.VideoProgressDTO;
import com.vijay.User_Master.Helper.ExceptionUtil;
import com.vijay.User_Master.service.VideoProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/video-progress")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Video Progress", description = "Video watching progress tracking")
public class VideoProgressController {

    private final VideoProgressService videoProgressService;

    @PutMapping("/{videoLessonId}")
    @Operation(summary = "Update video progress", 
               description = "Update watch time and last position for a video lesson")
    public ResponseEntity<?> updateProgress(
            @PathVariable Long videoLessonId,
            @RequestParam Integer currentPositionSeconds) {
        try {
            VideoProgressDTO progress = videoProgressService.updateProgress(videoLessonId, currentPositionSeconds);
            return ExceptionUtil.createBuildResponse(progress, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error updating video progress: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to update video progress: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/{videoLessonId}")
    @Operation(summary = "Get video progress", 
               description = "Get current user's progress for a video lesson")
    public ResponseEntity<?> getProgress(@PathVariable Long videoLessonId) {
        try {
            VideoProgressDTO progress = videoProgressService.getProgress(videoLessonId);
            if (progress == null) {
                return ExceptionUtil.createErrorResponseMessage(
                        "No progress found for this video",
                        HttpStatus.NOT_FOUND
                );
            }
            return ExceptionUtil.createBuildResponse(progress, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching video progress: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to fetch video progress",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get course progress", 
               description = "Get all video progress for a course")
    public ResponseEntity<?> getCourseProgress(@PathVariable Long courseId) {
        try {
            List<VideoProgressDTO> progressList = videoProgressService.getCourseProgress(courseId);
            return ExceptionUtil.createBuildResponse(progressList, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching course progress: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to fetch course progress",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PostMapping("/{videoLessonId}/complete")
    @Operation(summary = "Mark video as completed", 
               description = "Mark a video lesson as completed")
    public ResponseEntity<?> markAsCompleted(@PathVariable Long videoLessonId) {
        try {
            VideoProgressDTO progress = videoProgressService.markAsCompleted(videoLessonId);
            return ExceptionUtil.createBuildResponse(progress, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error marking video as completed: {}", e.getMessage(), e);
            return ExceptionUtil.createErrorResponseMessage(
                    "Failed to mark video as completed: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}

