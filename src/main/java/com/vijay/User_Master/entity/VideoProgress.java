package com.vijay.User_Master.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "video_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "video_lesson_id"})
})
public class VideoProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_lesson_id", nullable = false)
    private VideoLesson videoLesson;

    @Column(name = "watch_time_seconds")
    @Builder.Default
    private Integer watchTimeSeconds = 0; // Total time watched

    @Column(name = "last_position_seconds")
    @Builder.Default
    private Integer lastPositionSeconds = 0; // Last watched position (resume point)

    @Column(name = "is_completed")
    @Builder.Default
    private boolean isCompleted = false;

    @Column(name = "completion_percentage")
    @Builder.Default
    private Integer completionPercentage = 0; // 0-100

    @Column(name = "last_watched_at")
    private LocalDateTime lastWatchedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (lastWatchedAt == null) {
            lastWatchedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastWatchedAt = LocalDateTime.now();
        if (isCompleted && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }

    // Helper method to update progress
    public void updateProgress(int currentPositionSeconds, int videoDurationSeconds) {
        this.lastPositionSeconds = currentPositionSeconds;
        this.watchTimeSeconds = Math.max(this.watchTimeSeconds, currentPositionSeconds);
        
        if (videoDurationSeconds > 0) {
            this.completionPercentage = Math.min(100, (int) ((currentPositionSeconds * 100.0) / videoDurationSeconds));
            
            // Mark as completed if watched 90% or more
            if (this.completionPercentage >= 90) {
                this.isCompleted = true;
                if (this.completedAt == null) {
                    this.completedAt = LocalDateTime.now();
                }
            }
        }
    }
}

