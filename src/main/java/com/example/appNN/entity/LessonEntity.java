package com.example.appNN.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lesson")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user sở hữu bài học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUserEntity user;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "level")
    private String level;

    @Column(name = "lesson_index")
    private Integer lessonIndex;   // Bài thứ mấy của user trong khóa này

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Column(name = "review_stage")
    private Integer reviewStage;  // 0,1,2,3,...

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt;

    // Các từ trong bài
    @ManyToMany
    @JoinTable(
            name = "lesson_vocabulary",
            joinColumns = @JoinColumn(name = "lesson_id"),
            inverseJoinColumns = @JoinColumn(name = "vocab_id")
    )
    private Set<VocabularyEntity> vocabularies = new HashSet<>();
}
