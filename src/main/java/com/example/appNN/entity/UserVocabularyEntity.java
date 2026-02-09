package com.example.appNN.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Bảng từ vựng do người dùng tự tạo (từ điển riêng của user)
 * Khác với vocabulary (từ điển hệ thống)
 */
@Entity
@Table(name = "user_vocabulary")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVocabularyEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // User tạo từ này
    
    @Column(name = "word", nullable = false, length = 100)
    private String word; // Từ Hán
    
    @Column(name = "pronunciation", length = 200)
    private String pronunciation; // Phiên âm (pinyin)
    
    @Column(name = "pos", length = 50)
    private String pos; // Từ loại: n, v, adj, adv, etc.
    
    @Column(name = "meaning", nullable = false, columnDefinition = "TEXT")
    private String meaning; // Nghĩa tiếng Việt
    
    @Column(name = "example_src", columnDefinition = "TEXT")
    private String exampleSrc; // Câu ví dụ tiếng Trung
    
    @Column(name = "example_vi", columnDefinition = "TEXT")
    private String exampleVi; // Dịch câu ví dụ sang tiếng Việt
    
    @Column(name = "language_code", length = 10)
    private String languageCode; // cn, en, jp...
    
    @Column(name = "level", length = 10)
    private String level; // A1, A2, B1, B2...
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
