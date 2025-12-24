package com.vijay.User_Master.dto.tutorial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLessonDTO {
    private Long id;
    private String title;
    private String slug;
    private String videoUrl;
    private Integer durationSeconds;
    private Integer lessonOrder;
    private String summary;
    private Long courseId;
    private String courseTitle;
    private MultipartFile videoFile;
}
