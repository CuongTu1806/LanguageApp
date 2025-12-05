// com.example.appNN.entity.LessonQuizAttemptEntity
package com.example.appNN.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_quiz_attempt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonQuizAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user làm bài
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUserEntity user;

    // bài học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private LessonEntity lesson;

    @Column(name = "mode")
    private String mode; // lưu QuizMode.name()

    private int score;
    private int total;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
