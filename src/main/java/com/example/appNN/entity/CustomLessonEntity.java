package com.example.appNN.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lesson tùy chỉnh do người dùng tự tạo
 */
@Entity
@Table(name = "custom_lesson")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomLessonEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "language_code", length = 10)
    private String languageCode; // cn, en, jp, kr...
    
    @Column(name = "level", length = 10)
    private String level; // A1, A2, B1, B2...
    
    @Column(name = "vocabulary_count")
    private Integer vocabularyCount = 0; // Số lượng từ vựng trong bài
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Quan hệ với vocabulary thông qua bảng trung gian
    @ManyToMany
    @JoinTable(
        name = "custom_lesson_vocabulary",
        joinColumns = @JoinColumn(name = "custom_lesson_id"),
        inverseJoinColumns = @JoinColumn(name = "vocabulary_id")
    )
    private List<VocabularyEntity> vocabularies = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
