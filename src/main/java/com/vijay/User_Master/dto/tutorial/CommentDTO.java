package com.vijay.User_Master.dto.tutorial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {
    private Long id;
    private String content;
    private String authorUsername;
    private Long authorId;
    private String createdAt;
    private String updatedAt;
    private boolean isEdited;
    private boolean isOwner; // Current user owns this comment
    private int replyCount;
    private List<CommentDTO> replies;
}
