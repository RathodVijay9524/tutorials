package com.vijay.User_Master.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    @JsonBackReference
    private Quiz quiz;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_type", length = 50)
    @Builder.Default
    private String questionType = "MULTIPLE_CHOICE"; // MULTIPLE_CHOICE, TRUE_FALSE, CODE

    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet; // For code-based questions

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation; // Shown after answering

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "points")
    @Builder.Default
    private Integer points = 1;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private Set<QuestionOption> options = new HashSet<>();

    // Helper method to get the correct option
    public QuestionOption getCorrectOption() {
        return options.stream()
                .filter(QuestionOption::isCorrect)
                .findFirst()
                .orElse(null);
    }
}
