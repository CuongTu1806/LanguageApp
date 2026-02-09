package com.example.appNN.repository;

import com.example.appNN.entity.UserVocabularyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho user_vocabulary (từ điển riêng của user)
 */
@Repository
public interface UserVocabularyRepository extends JpaRepository<UserVocabularyEntity, Long> {
    
    /**
     * Tìm từ theo user_id, word và languageCode
     */
    Optional<UserVocabularyEntity> findByUserIdAndWordAndLanguageCode(Long userId, String word, String languageCode);
    
    /**
     * Lấy tất cả từ vựng của một user
     */
    List<UserVocabularyEntity> findByUserId(Long userId);
    
    /**
     * Lấy từ vựng của user theo ngôn ngữ
     */
    List<UserVocabularyEntity> findByUserIdAndLanguageCode(Long userId, String languageCode);
    
    /**
     * Lấy từ vựng của user theo ngôn ngữ và level
     */
    List<UserVocabularyEntity> findByUserIdAndLanguageCodeAndLevel(Long userId, String languageCode, String level);
}
