package com.example.appNN.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity cho bảng trung gian lesson_vocabulary
 * Hỗ trợ cả vocabulary (từ hệ thống) và user_vocabulary (từ user tự tạo)
 */
@Entity
@Table(name = "lesson_vocabulary")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonVocabularyEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;
    
    @Column(name = "vocab_id")
    private Long vocabId; // NULL nếu là user vocab
    
    @Column(name = "user_vocab_id")
    private Long userVocabId; // NULL nếu là system vocab
    
    // Relationships để fetch data
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocab_id", insertable = false, updatable = false)
    private VocabularyEntity vocabulary;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_vocab_id", insertable = false, updatable = false)
    private UserVocabularyEntity userVocabulary;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", insertable = false, updatable = false)
    private LessonEntity lesson;
}
