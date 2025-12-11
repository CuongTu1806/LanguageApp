package com.example.appNN.repository;

import com.example.appNN.entity.CustomLessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomLessonRepository extends JpaRepository<CustomLessonEntity, Long> {
    
    /**
     * Tìm tất cả custom lesson của một user
     */
    List<CustomLessonEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Tìm custom lesson theo user và language/level
     */
    List<CustomLessonEntity> findByUserIdAndLanguageCodeAndLevel(Long userId, String languageCode, String level);
    
    /**
     * Tìm kiếm custom lesson theo số lượng từ
     */
    List<CustomLessonEntity> findByUserIdAndVocabularyCountBetween(
            Long userId, Integer minCount, Integer maxCount
    );
    
    List<CustomLessonEntity> findByUserIdAndVocabularyCountGreaterThanEqual(
            Long userId, Integer minCount
    );
    
    List<CustomLessonEntity> findByUserIdAndVocabularyCountLessThanEqual(
            Long userId, Integer maxCount
    );
    
    /**
     * Tìm tất cả custom lesson của một user
     */
    List<CustomLessonEntity> findByUserId(Long userId);
}
