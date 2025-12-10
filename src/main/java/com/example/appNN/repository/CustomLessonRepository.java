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
}
