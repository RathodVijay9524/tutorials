package com.vijay.User_Master.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    @JsonBackReference
    private Quiz quiz;

    @Column(name = "score")
    private Integer score; // Points earned

    @Column(name = "max_score")
    private Integer maxScore; // Total possible points

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "is_passed")
    @Builder.Default
    private boolean isPassed = false;

    @Column(name = "is_completed")
    @Builder.Default
    private boolean isCompleted = false;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "quizAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private Set<QuizResponse> responses = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
    }

    // Helper method to complete the attempt
    public void completeAttempt(int earnedScore, int totalScore, int passingPercentage) {
        this.score = earnedScore;
        this.maxScore = totalScore;
        this.percentage = totalScore > 0 ? (earnedScore * 100.0 / totalScore) : 0.0;
        this.isPassed = this.percentage >= passingPercentage;
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
    }
}
