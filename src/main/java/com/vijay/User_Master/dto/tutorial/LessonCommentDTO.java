package com.vijay.User_Master.dto.tutorial;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonCommentDTO {
    
    private Long id;
    private Long lessonId;
    private String lessonTitle;
    private Long authorId;
    private String authorName;
    private String authorImageName;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isEdited;
    private Long parentCommentId;
    
    @Builder.Default
    private List<LessonCommentDTO> replies = new ArrayList<>();
    
    // Helper to check if user can edit/delete
    private boolean canModify;
}
