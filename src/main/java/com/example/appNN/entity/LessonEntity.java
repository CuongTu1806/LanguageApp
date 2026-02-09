package com.example.appNN.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @Column(name = "display_order")
    private Integer displayOrder;  // Số thứ tự hiển thị (1, 2, 3...)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "vocabulary_count")
    private Integer vocabularyCount = 0; // Số lượng từ vựng trong bài

    @Column(name = "review_stage")
    private Integer reviewStage;  // 0,1,2,3,...

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt;

    @Column(name = "lesson_type", length = 20)
    private String lessonType = "system"; // 'system' hoặc 'personal'

    @Column(name = "title", length = 200)
    private String title; // Tiêu đề bài học (cho personal lesson)

    @Column(name = "description", length = 500)
    private String description; // Mô tả bài học (cho personal lesson)

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Các từ trong bài - không dùng ManyToMany nữa vì cần hỗ trợ user_vocabulary
    // Sẽ query qua LessonVocabularyRepository
    @Transient
    public Set<VocabularyEntity> getVocabularies() {
        // Placeholder - sẽ query qua service
        return new HashSet<>();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
