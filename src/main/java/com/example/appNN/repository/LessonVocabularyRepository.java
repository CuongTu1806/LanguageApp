package com.example.appNN.repository;

import com.example.appNN.entity.LessonVocabularyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonVocabularyRepository extends JpaRepository<LessonVocabularyEntity, Long> {
    
    /**
     * Tìm tất cả vocabulary trong một lesson
     */
    List<LessonVocabularyEntity> findByLessonId(Long lessonId);
    
    /**
     * Tìm vocabulary mapping cụ thể (system vocab)
     */
    Optional<LessonVocabularyEntity> findByLessonIdAndVocabId(Long lessonId, Long vocabId);
    
    /**
     * Tìm vocabulary mapping cụ thể (user vocab)
     */
    Optional<LessonVocabularyEntity> findByLessonIdAndUserVocabId(Long lessonId, Long userVocabId);
    
    /**
     * Xóa tất cả vocabulary của một lesson
     */
    void deleteByLessonId(Long lessonId);
    
    /**
     * Đếm số lượng vocabulary trong lesson
     */
    long countByLessonId(Long lessonId);
}
